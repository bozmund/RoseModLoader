package rose.boot.mojang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/** Downloads files and checks their SHA-1, the hash Mojang publishes for every game file. */
public final class Downloader {
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String getString(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(request(url), HttpResponse.BodyHandlers.ofString());
        checkStatus(url, response.statusCode());
        return response.body();
    }

    /**
     * Downloads {@code url} to {@code target} unless a file with the expected hash is already there.
     *
     * @param sha1 expected SHA-1 in hex, or {@code null} to skip verification
     */
    public void download(String url, Path target, String sha1) throws IOException, InterruptedException {
        download(url, target, sha1, false);
    }

    /** As {@link #download(String, Path, String)}; {@code quiet} suppresses per-file log lines (e.g. for assets). */
    public void download(String url, Path target, String sha1, boolean quiet) throws IOException, InterruptedException {
        if (Files.exists(target) && (sha1 == null || sha1.equalsIgnoreCase(sha1(target)))) {
            if (!quiet) System.out.println("[rose] cached  " + target);
            return;
        }
        if (!quiet) System.out.println("[rose] fetch   " + url);
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".part");
        HttpResponse<InputStream> response = http.send(request(url), HttpResponse.BodyHandlers.ofInputStream());
        checkStatus(url, response.statusCode());
        try (InputStream in = response.body()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        if (sha1 != null) {
            String actual = sha1(tmp);
            if (!sha1.equalsIgnoreCase(actual)) {
                Files.deleteIfExists(tmp);
                throw new IOException("SHA-1 mismatch for " + url + ": expected " + sha1 + ", got " + actual);
            }
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) > 0) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static HttpRequest request(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "RoseModLoader-corpus-tools")
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    private static void checkStatus(String url, int status) throws IOException {
        if (status / 100 != 2) throw new IOException("HTTP " + status + " for " + url);
    }
}

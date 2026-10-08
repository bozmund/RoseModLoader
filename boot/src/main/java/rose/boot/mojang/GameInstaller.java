package rose.boot.mojang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Downloads and verifies everything needed to run one Minecraft version: game jars, libraries and assets.
 * Files go into a shared runtime folder ({@code run/} in development) laid out like the official launcher's.
 */
public final class GameInstaller {
    private static final String RESOURCES_URL = "https://resources.download.minecraft.net/";

    private final Path root;
    private final Downloader downloader = new Downloader();
    private final VersionManifest manifest = new VersionManifest(downloader);

    public GameInstaller(Path root) {
        this.root = root;
    }

    public Path librariesDir() { return root.resolve("libraries"); }

    public Path assetsDir() { return root.resolve("assets"); }

    public InstalledGame installClient(String version) throws IOException, InterruptedException {
        JsonObject json = versionJson(version);
        Path jar = versionDir(version).resolve("client.jar");
        downloadEntry(json.getAsJsonObject("downloads").getAsJsonObject("client"), jar);

        List<Path> libraries = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("libraries")) {
            JsonObject library = element.getAsJsonObject();
            if (!Rules.allowed(library)) continue;
            JsonObject artifact = library.getAsJsonObject("downloads").getAsJsonObject("artifact");
            if (artifact == null) continue;
            Path target = librariesDir().resolve(artifact.get("path").getAsString());
            downloadEntry(artifact, target);
            libraries.add(target);
        }

        String assetIndex = installAssets(json.getAsJsonObject("assetIndex"));
        return new InstalledGame(version, InstalledGame.Side.CLIENT, jar, List.copyOf(libraries),
                json.get("mainClass").getAsString(), json, assetIndex);
    }

    public InstalledGame installServer(String version) throws IOException, InterruptedException {
        JsonObject json = versionJson(version);
        Path bundler = versionDir(version).resolve("server-bundler.jar");
        downloadEntry(json.getAsJsonObject("downloads").getAsJsonObject("server"), bundler);

        // Since 1.18 the server download is a "bundler": the real server jar and its libraries are inside it.
        Path serverJar = versionDir(version).resolve("server.jar");
        List<Path> libraries = new ArrayList<>();
        String mainClass;
        try (FileSystem zip = FileSystems.newFileSystem(URI.create("jar:" + bundler.toUri()), Map.of())) {
            Path meta = zip.getPath("META-INF");
            extractListed(meta, "versions", List.of(serverJar));
            for (String line : Files.readAllLines(meta.resolve("libraries.list"))) {
                String path = line.split("\t")[2]; // <sha256>\t<maven id>\t<path>
                Path target = librariesDir().resolve(path);
                if (!Files.exists(target)) {
                    Files.createDirectories(target.getParent());
                    Files.copy(meta.resolve("libraries").resolve(path), target, StandardCopyOption.REPLACE_EXISTING);
                }
                libraries.add(target);
            }
            mainClass = Files.readString(meta.resolve("main-class")).trim();
        }
        return new InstalledGame(version, InstalledGame.Side.SERVER, serverJar, List.copyOf(libraries),
                mainClass, json, null);
    }

    private static void extractListed(Path meta, String folder, List<Path> targets) throws IOException {
        List<String> lines = Files.readAllLines(meta.resolve(folder + ".list"));
        for (int i = 0; i < targets.size(); i++) {
            Path target = targets.get(i);
            if (Files.exists(target)) continue;
            String path = lines.get(i).split("\t")[2];
            Files.copy(meta.resolve(folder).resolve(path), target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Downloads the asset index and every object it lists. Returns the index id. */
    private String installAssets(JsonObject assetIndex) throws IOException, InterruptedException {
        String id = assetIndex.get("id").getAsString();
        Path indexFile = assetsDir().resolve("indexes").resolve(id + ".json");
        downloadEntry(assetIndex, indexFile);
        JsonObject objects = JsonParser.parseString(Files.readString(indexFile)).getAsJsonObject().getAsJsonObject("objects");

        List<String> missing = new ArrayList<>();
        for (var entry : objects.entrySet()) {
            String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
            if (!Files.exists(assetObject(hash))) missing.add(hash);
        }
        if (missing.isEmpty()) return id;

        System.out.println("[rose] downloading " + missing.size() + " asset files");
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            List<Future<?>> futures = new ArrayList<>();
            for (String hash : missing) {
                String url = RESOURCES_URL + hash.substring(0, 2) + "/" + hash;
                futures.add(pool.submit(() -> {
                    downloader.download(url, assetObject(hash), hash, true);
                    return null;
                }));
            }
            for (Future<?> future : futures) future.get();
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IOException("asset download failed", e.getCause());
        }
        return id;
    }

    private Path assetObject(String hash) {
        return assetsDir().resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
    }

    private JsonObject versionJson(String version) throws IOException, InterruptedException {
        return manifest.versionJson(version, versionDir(version).resolve(version + ".json"));
    }

    private Path versionDir(String version) {
        return root.resolve("versions").resolve(version);
    }

    private void downloadEntry(JsonObject entry, Path target) throws IOException, InterruptedException {
        downloader.download(entry.get("url").getAsString(), target, entry.get("sha1").getAsString());
    }
}

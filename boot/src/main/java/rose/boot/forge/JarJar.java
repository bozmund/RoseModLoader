package rose.boot.forge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Forge's jar-in-jar: a mod can carry other mods and libraries in {@code META-INF/jarjar/}, listed in
 * {@code metadata.json}. They are extracted next to the translation cache and loaded like jars in the mods folder.
 * When several jars carry the same artifact, the highest version wins (Forge resolves a range; the versions old mods
 * nest are meant to be compatible). A nested artifact is skipped when the mods folder has that mod itself, or when
 * Rose provides it (MixinExtras).
 */
public final class JarJar {
    private static final String METADATA = "META-INF/jarjar/metadata.json";
    /** Artifacts Rose provides itself (its launcher carries MixinExtras). */
    private static final java.util.Set<String> PROVIDED_GROUPS = java.util.Set.of("io.github.llamalad7");

    private record Nested(String artifact, String version, Path jar) {}

    private JarJar() {}

    /** {@code jars} followed by the jars they nest (recursively), extracted into {@code dir}. */
    public static List<Path> expand(List<Path> jars, Path dir) throws IOException {
        Map<String, Nested> chosen = new LinkedHashMap<>();
        List<Path> pending = new ArrayList<>(jars);
        java.util.Set<String> installed = new java.util.HashSet<>();
        for (Path jar : jars) installed.add(ForgeMods.modIdOrNull(jar));
        while (!pending.isEmpty()) {
            Path jar = pending.removeFirst();
            for (Nested nested : nestedIn(jar, dir)) {
                String id = ForgeMods.modIdOrNull(nested.jar());
                if (id != null && installed.contains(id)) continue;
                Nested previous = chosen.get(nested.artifact());
                if (previous != null && compare(previous.version(), nested.version()) >= 0) continue;
                chosen.put(nested.artifact(), nested);
                pending.add(nested.jar());
            }
        }
        List<Path> out = new ArrayList<>(jars);
        chosen.values().forEach(n -> out.add(n.jar()));
        return out;
    }

    private static List<Nested> nestedIn(Path jar, Path dir) throws IOException {
        List<Nested> out = new ArrayList<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry metadata = zip.getEntry(METADATA);
            if (metadata == null) return out;
            JsonObject root;
            try (InputStream in = zip.getInputStream(metadata)) {
                root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
            for (JsonElement e : root.getAsJsonArray("jars")) {
                JsonObject entry = e.getAsJsonObject();
                JsonObject identifier = entry.getAsJsonObject("identifier");
                if (PROVIDED_GROUPS.contains(identifier.get("group").getAsString())) continue;
                String artifact = identifier.get("group").getAsString() + ":" + identifier.get("artifact").getAsString();
                String version = entry.getAsJsonObject("version").get("artifactVersion").getAsString();
                ZipEntry nested = zip.getEntry(entry.get("path").getAsString());
                if (nested == null) continue;
                Path target = dir.resolve(Path.of(nested.getName()).getFileName().toString());
                if (!Files.exists(target)) {
                    Files.createDirectories(dir);
                    Path tmp = Files.createTempFile(dir, "jarjar-", ".part");
                    try (InputStream in = zip.getInputStream(nested)) {
                        Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                }
                out.add(new Nested(artifact, version, target));
            }
        }
        return out;
    }

    /** Compares dotted versions number by number ({@code 0.13.15+1.20.1}: the build suffix after '+' is ignored). */
    static int compare(String a, String b) {
        String[] x = a.split("\\+")[0].split("[.-]");
        String[] y = b.split("\\+")[0].split("[.-]");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            String p = i < x.length ? x[i] : "0";
            String q = i < y.length ? y[i] : "0";
            int c = p.matches("\\d+") && q.matches("\\d+") ? Long.compare(Long.parseLong(p), Long.parseLong(q)) : p.compareTo(q);
            if (c != 0) return c;
        }
        return 0;
    }
}

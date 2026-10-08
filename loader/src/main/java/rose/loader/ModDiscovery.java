package rose.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Finds Rose-native mods: jars in a mods folder, plus extra jars or folders (for development) passed directly.
 * A jar or folder is a mod if it contains {@value ModMetadata#FILE_NAME} at its root.
 */
public final class ModDiscovery {
    private ModDiscovery() {}

    public static List<ModMetadata> discover(Path modsDir, List<Path> extraRoots) throws IOException {
        List<Path> candidates = new ArrayList<>(extraRoots);
        if (Files.isDirectory(modsDir)) {
            try (Stream<Path> files = Files.list(modsDir)) {
                files.filter(p -> p.getFileName().toString().endsWith(".jar")).sorted().forEach(candidates::add);
            }
        }

        List<ModMetadata> mods = new ArrayList<>();
        Map<String, Path> seen = new HashMap<>();
        for (Path candidate : candidates) {
            ModMetadata mod = read(candidate);
            if (mod == null) continue;
            Path previous = seen.putIfAbsent(mod.id(), candidate);
            if (previous != null) {
                throw new IOException("Duplicate mod id '" + mod.id() + "' in " + previous + " and " + candidate);
            }
            mods.add(mod);
        }
        return List.copyOf(mods);
    }

    /** Returns the mod's metadata, or {@code null} if this jar/folder isn't a Rose-native mod. */
    static ModMetadata read(Path root) throws IOException {
        if (Files.isDirectory(root)) return parse(root.resolve(ModMetadata.FILE_NAME), root);
        try (FileSystem jar = FileSystems.newFileSystem(URI.create("jar:" + root.toUri()), Map.of())) {
            return parse(jar.getPath(ModMetadata.FILE_NAME), root);
        }
    }

    private static ModMetadata parse(Path file, Path root) throws IOException {
        if (!Files.exists(file)) return null;
        JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        int schema = json.has("schemaVersion") ? json.get("schemaVersion").getAsInt() : 1;
        if (schema != 1) throw new IOException(root + ": unsupported rose.mod.json schemaVersion " + schema);

        String id = required(json, "id", root);
        if (!id.matches("[a-z][a-z0-9_]{1,63}")) {
            throw new IOException(root + ": mod id '" + id + "' must be 2-64 chars of a-z, 0-9, _ and start with a letter");
        }
        return new ModMetadata(
                id,
                json.has("name") ? json.get("name").getAsString() : id,
                required(json, "version", root),
                strings(json.getAsJsonArray("mixins")),
                entrypoints(json.getAsJsonObject("entrypoints")),
                root);
    }

    private static Map<String, List<String>> entrypoints(JsonObject json) {
        if (json == null) return Map.of();
        Map<String, List<String>> out = new HashMap<>();
        for (var entry : json.entrySet()) out.put(entry.getKey(), strings(entry.getValue().getAsJsonArray()));
        return Map.copyOf(out);
    }

    private static String required(JsonObject json, String key, Path root) throws IOException {
        if (!json.has(key)) throw new IOException(root + ": rose.mod.json is missing \"" + key + "\"");
        return json.get(key).getAsString();
    }

    private static List<String> strings(JsonArray array) {
        if (array == null) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonElement e : array) out.add(e.getAsString());
        return List.copyOf(out);
    }

    /** Parses a path list such as the {@code rose.dev.mods} system property. */
    public static List<Path> parsePathList(String value) {
        if (value == null || value.isBlank()) return List.of();
        List<Path> paths = new ArrayList<>();
        for (String part : value.split(java.io.File.pathSeparator)) {
            if (!part.isBlank()) paths.add(Path.of(part.trim()));
        }
        return List.copyOf(paths);
    }
}

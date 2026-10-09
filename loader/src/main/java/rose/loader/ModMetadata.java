package rose.loader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * A loaded mod. Rose-native mods describe themselves in {@code rose.mod.json}; mods for other loaders (e.g. Forge
 * 1.20.1) are discovered by their own metadata and run through a translated copy of their jar.
 *
 * <p>{@code rose.mod.json}:</p>
 *
 * <pre>
 * {
 *   "schemaVersion": 1,
 *   "id": "hello",
 *   "name": "Hello Rose",
 *   "version": "0.0.1",
 *   "mixins": ["hello.mixins.json"],
 *   "accessWidener": "hello.accesswidener",
 *   "entrypoints": {
 *     "main": ["com.example.HelloMod"],
 *     "client": ["com.example.HelloClient"]
 *   }
 * }
 * </pre>
 *
 * @param entrypoints entrypoint kind ({@code main}, {@code client}, ...) to class names
 * @param root        the jar or folder the game loads (for translated mods: the translated jar)
 * @param dialect     {@link #NATIVE} or the source environment, e.g. {@code forge-1.20.1}
 * @param originalJar the jar as the user installed it
 * @param accessWidener path of an {@link AccessWidener} file inside the mod, or {@code null}
 */
public record ModMetadata(String id, String name, String version, List<String> mixins,
                          Map<String, List<String>> entrypoints, Path root, String dialect, Path originalJar,
                          String accessWidener) {
    public static final String FILE_NAME = "rose.mod.json";
    /** Mods written for Rose itself. */
    public static final String NATIVE = "rose";

    /** A Rose-native mod (its jar is used as-is). */
    public ModMetadata(String id, String name, String version, List<String> mixins,
                       Map<String, List<String>> entrypoints, Path root) {
        this(id, name, version, mixins, entrypoints, root, NATIVE, root, null);
    }

    /** A mod translated from another loader. */
    public ModMetadata(String id, String name, String version, List<String> mixins,
                       Map<String, List<String>> entrypoints, Path root, String dialect, Path originalJar) {
        this(id, name, version, mixins, entrypoints, root, dialect, originalJar, null);
    }

    public boolean isNative() {
        return NATIVE.equals(dialect);
    }

    public List<String> entrypoints(String kind) {
        return entrypoints.getOrDefault(kind, List.of());
    }
}

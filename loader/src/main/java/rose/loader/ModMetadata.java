package rose.loader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * A Rose-native mod as described by its {@code rose.mod.json}.
 *
 * <pre>
 * {
 *   "schemaVersion": 1,
 *   "id": "hello",
 *   "name": "Hello Rose",
 *   "version": "0.0.1",
 *   "mixins": ["hello.mixins.json"],
 *   "entrypoints": {
 *     "main": ["com.example.HelloMod"],
 *     "client": ["com.example.HelloClient"]
 *   }
 * }
 * </pre>
 *
 * @param entrypoints entrypoint kind ({@code main}, {@code client}, ...) to class names
 * @param root        the mod's jar or folder
 */
public record ModMetadata(String id, String name, String version, List<String> mixins,
                          Map<String, List<String>> entrypoints, Path root) {
    public static final String FILE_NAME = "rose.mod.json";

    public List<String> entrypoints(String kind) {
        return entrypoints.getOrDefault(kind, List.of());
    }
}

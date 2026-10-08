package rose.loader;

import java.nio.file.Path;
import java.util.List;

/**
 * A Rose-native mod as described by its {@code rose.mod.json}.
 *
 * <pre>
 * {
 *   "schemaVersion": 1,
 *   "id": "hello",
 *   "name": "Hello Rose",
 *   "version": "0.0.1",
 *   "mixins": ["hello.mixins.json"]
 * }
 * </pre>
 *
 * @param root the mod's jar or folder
 */
public record ModMetadata(String id, String name, String version, List<String> mixins, Path root) {
    public static final String FILE_NAME = "rose.mod.json";
}

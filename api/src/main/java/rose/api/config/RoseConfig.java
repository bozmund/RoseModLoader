package rose.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Simple per-mod JSON config in {@code config/<modid>.json}, mapped onto a plain class with public fields.
 * Missing files are created from the defaults; missing keys keep their default values.
 *
 * <pre>
 * public class MyConfig { public int maxCount = 10; }
 * MyConfig config = RoseConfig.load("mymod", MyConfig.class, MyConfig::new);
 * </pre>
 */
public final class RoseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static <T> T load(String modId, Class<T> type, Supplier<T> defaults) {
        return load(Path.of("config"), modId, type, defaults);
    }

    static <T> T load(Path configDir, String modId, Class<T> type, Supplier<T> defaults) {
        Path file = configDir.resolve(modId + ".json");
        try {
            T value = defaults.get();
            if (Files.exists(file)) {
                T read = GSON.fromJson(Files.readString(file), type);
                if (read != null) value = read;
            }
            // Write back so new keys (with their defaults) appear in the file.
            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(value));
            return value;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config " + file, e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Config " + file + " is not valid JSON: " + e.getMessage(), e);
        }
    }

    private RoseConfig() {}
}

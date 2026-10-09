package rose.testing.oracle;

import com.google.gson.JsonElement;

/**
 * One way Rose's dump differs from the reference.
 *
 * @param file     dump file without {@code .json} ({@code items}, {@code recipes}, ...)
 * @param key      top-level key in that file: an id, or {@code registry #tag} for tags
 * @param path     where inside the value ({@code /components/minecraft:food/nutrition}); for registries and tags,
 *                 the member id; empty when the whole entry is missing or extra
 * @param expected the reference's value, or {@code null}
 * @param actual   Rose's value, or {@code null}
 */
public record Difference(String file, String key, String path, Kind kind, JsonElement expected, JsonElement actual) {
    public enum Kind {
        /** The reference has it, Rose doesn't. */
        MISSING,
        /** Rose has it, the reference doesn't. */
        EXTRA,
        /** Both have it with different values. */
        CHANGED
    }

    /** The string allow-list patterns match against: {@code file key path}. */
    public String id() {
        return path.isEmpty() ? file + " " + key : file + " " + key + " " + path;
    }
}

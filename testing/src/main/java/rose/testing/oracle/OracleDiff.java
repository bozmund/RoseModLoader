package rose.testing.oracle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Compares a dump made on Rose with one made on the reference server (the mods' native 26.3 ports). The reference
 * is the answer key: anything it has that Rose lacks is {@link Difference.Kind#MISSING}.
 */
public final class OracleDiff {
    /** The files {@code OracleDump} writes. Lists in the first two are sets (sorted ids), elsewhere they're ordered. */
    public static final List<String> FILES = List.of("registries", "tags", "blocks", "items", "recipes", "loot_tables");
    private static final Set<String> SET_FILES = Set.of("registries", "tags");

    public static List<Difference> compare(Path roseDir, Path referenceDir) throws IOException {
        List<Difference> out = new ArrayList<>();
        for (String file : FILES) {
            out.addAll(compare(file, read(roseDir.resolve(file + ".json")), read(referenceDir.resolve(file + ".json"))));
        }
        return out;
    }

    private static JsonObject read(Path file) throws IOException {
        if (!Files.exists(file)) throw new IOException("dump file missing: " + file);
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }

    public static List<Difference> compare(String file, JsonObject rose, JsonObject reference) {
        List<Difference> out = new ArrayList<>();
        Set<String> keys = new TreeSet<>(reference.keySet());
        keys.addAll(rose.keySet());
        for (String key : keys) {
            JsonElement expected = reference.get(key);
            JsonElement actual = rose.get(key);
            if (actual == null) {
                out.add(new Difference(file, key, "", Difference.Kind.MISSING, expected, null));
            } else if (expected == null) {
                out.add(new Difference(file, key, "", Difference.Kind.EXTRA, null, actual));
            } else if (SET_FILES.contains(file) && expected.isJsonArray() && actual.isJsonArray()) {
                compareSets(file, key, actual.getAsJsonArray(), expected.getAsJsonArray(), out);
            } else {
                compareValues(file, key, "", actual, expected, out);
            }
        }
        return out;
    }

    private static void compareSets(String file, String key, JsonArray actual, JsonArray expected, List<Difference> out) {
        Set<String> have = strings(actual);
        Set<String> want = strings(expected);
        for (String member : want) {
            if (!have.contains(member)) out.add(new Difference(file, key, member, Difference.Kind.MISSING, null, null));
        }
        for (String member : have) {
            if (!want.contains(member)) out.add(new Difference(file, key, member, Difference.Kind.EXTRA, null, null));
        }
    }

    private static Set<String> strings(JsonArray array) {
        Set<String> out = new TreeSet<>();
        array.forEach(e -> out.add(e.getAsString()));
        return out;
    }

    /** Walks both values and reports the deepest paths that differ, so one changed field doesn't hide the rest. */
    private static void compareValues(String file, String key, String path, JsonElement actual, JsonElement expected, List<Difference> out) {
        if (actual.equals(expected)) return;
        if (isError(actual) || isError(expected)) {
            out.add(new Difference(file, key, path, Difference.Kind.CHANGED, expected, actual));
            return;
        }
        if (actual.isJsonObject() && expected.isJsonObject()) {
            JsonObject a = actual.getAsJsonObject();
            JsonObject e = expected.getAsJsonObject();
            Set<String> fields = new TreeSet<>(e.keySet());
            fields.addAll(a.keySet());
            for (String field : fields) {
                String child = path + "/" + field;
                if (!a.has(field)) {
                    out.add(new Difference(file, key, child, Difference.Kind.MISSING, e.get(field), null));
                } else if (!e.has(field)) {
                    out.add(new Difference(file, key, child, Difference.Kind.EXTRA, null, a.get(field)));
                } else {
                    compareValues(file, key, child, a.get(field), e.get(field), out);
                }
            }
            return;
        }
        if (actual.isJsonArray() && expected.isJsonArray() && actual.getAsJsonArray().size() == expected.getAsJsonArray().size()) {
            JsonArray a = actual.getAsJsonArray();
            JsonArray e = expected.getAsJsonArray();
            for (int i = 0; i < a.size(); i++) {
                compareValues(file, key, path + "/" + i, a.get(i), e.get(i), out);
            }
            return;
        }
        out.add(new Difference(file, key, path, Difference.Kind.CHANGED, expected, actual));
    }

    /** The dumper writes {@code {"$error": "..."}} where a value couldn't be encoded. */
    static boolean isError(JsonElement value) {
        return value.isJsonObject() && value.getAsJsonObject().has("$error");
    }

    private OracleDiff() {}
}

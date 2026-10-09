package rose.testing.oracle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The oracle comparison's result, as Markdown for people and JSON for the foundry. */
public final class OracleReport {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int MAX_VALUE_LENGTH = 300;

    private final String title;
    private final List<Difference> open = new ArrayList<>();
    private final Map<Difference, AllowList.Entry> allowed = new LinkedHashMap<>();
    private final List<AllowList.Entry> unusedAllows = new ArrayList<>();

    public OracleReport(String title, List<Difference> differences, AllowList allowList) {
        this.title = title;
        Set<AllowList.Entry> used = new LinkedHashSet<>();
        for (Difference d : differences) {
            Optional<AllowList.Entry> entry = allowList.match(d);
            if (entry.isPresent()) {
                allowed.put(d, entry.get());
                used.add(entry.get());
            } else {
                open.add(d);
            }
        }
        for (AllowList.Entry e : allowList.entries()) {
            if (!used.contains(e)) unusedAllows.add(e);
        }
    }

    /** Differences no allow-list entry explains. The comparison is clean when this is empty. */
    public List<Difference> open() {
        return open;
    }

    public String markdown() {
        StringBuilder md = new StringBuilder();
        md.append("# Oracle comparison: ").append(title).append("\n\n");
        md.append("Rose's dump compared with the reference server's (the native 26.3 port is the answer key).\n\n");
        md.append("| File | Open | Allowed |\n|---|---:|---:|\n");
        for (String file : OracleDiff.FILES) {
            long o = open.stream().filter(d -> d.file().equals(file)).count();
            long a = allowed.keySet().stream().filter(d -> d.file().equals(file)).count();
            md.append("| ").append(file).append(" | ").append(o).append(" | ").append(a).append(" |\n");
        }
        md.append("| **total** | **").append(open.size()).append("** | **").append(allowed.size()).append("** |\n\n");

        md.append("**Kinds:** MISSING = the reference has it, Rose doesn't. EXTRA = only Rose has it. CHANGED = both, different values.\n\n");

        md.append("## Open differences\n\n");
        if (open.isEmpty()) md.append("None.\n\n");
        String lastHeading = null;
        for (Difference d : open) {
            String heading = d.file() + ": `" + d.key() + "`";
            if (!heading.equals(lastHeading)) {
                md.append("### ").append(heading).append("\n\n");
                lastHeading = heading;
            }
            md.append("- ").append(d.kind());
            if (!d.path().isEmpty()) md.append(" `").append(d.path()).append("`");
            if (d.expected() != null) md.append("\n  - reference: `").append(shorten(d.expected())).append("`");
            if (d.actual() != null) md.append("\n  - rose: `").append(shorten(d.actual())).append("`");
            md.append("\n");
        }
        if (!open.isEmpty()) md.append("\n");

        md.append("## Allowed differences\n\n");
        if (allowed.isEmpty()) md.append("None.\n\n");
        allowed.forEach((d, e) -> md.append("- ").append(d.kind()).append(" `").append(d.id()).append("`: ").append(e.reason()).append("\n"));
        if (!allowed.isEmpty()) md.append("\n");

        if (!unusedAllows.isEmpty()) {
            md.append("## Allow-list entries that matched nothing\n\nRemove them, or fix their pattern.\n\n");
            unusedAllows.forEach(e -> md.append("- `").append(e.pattern()).append("`: ").append(e.reason()).append("\n"));
            md.append("\n");
        }
        return md.toString();
    }

    public String json() {
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("open", open.size());
        json.addProperty("allowed", allowed.size());
        JsonArray differences = new JsonArray();
        open.forEach(d -> differences.add(toJson(d, null)));
        allowed.forEach((d, e) -> differences.add(toJson(d, e)));
        json.add("differences", differences);
        JsonArray unused = new JsonArray();
        unusedAllows.forEach(e -> unused.add(e.pattern()));
        json.add("unused_allows", unused);
        return GSON.toJson(json) + "\n";
    }

    private static JsonObject toJson(Difference d, AllowList.Entry allow) {
        JsonObject json = new JsonObject();
        json.addProperty("id", d.id());
        json.addProperty("file", d.file());
        json.addProperty("key", d.key());
        json.addProperty("path", d.path());
        json.addProperty("kind", d.kind().name());
        if (d.expected() != null) json.add("reference", d.expected());
        if (d.actual() != null) json.add("rose", d.actual());
        if (allow != null) json.addProperty("allowed", allow.reason());
        return json;
    }

    private static String shorten(JsonElement value) {
        String s = value.toString().replace('`', '\'');
        return s.length() <= MAX_VALUE_LENGTH ? s : s.substring(0, MAX_VALUE_LENGTH) + "...";
    }
}

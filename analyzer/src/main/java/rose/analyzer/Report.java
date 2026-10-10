package rose.analyzer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import rose.analyzer.Finding.Status;

/**
 * The result of analyzing one mod: JSON for tools and the foundry, Markdown for people.
 *
 * <p>Findings are grouped by context (see {@link ClassContexts}): <b>runtime</b> problems stop the mod from working;
 * <b>integration</b> problems only matter when an optional mod (JEI, EMI...) is installed; <b>data-generation</b>
 * problems only affect the mod author's build-time generators.
 */
public record Report(Path jar, String modId, String source, int classes, int references, int redirected, int adapted, int bridged,
                     List<Finding> findings, Map<String, Integer> forgeSurface, List<String> nestedJars,
                     ClassContexts contexts) {

    public static final List<String> CONTEXTS = List.of("runtime", "integration", "data-generation");
    private static final int USED_IN_LIMIT = 5;

    public Map<Status, Integer> countByStatus(String context) {
        Map<Status, Integer> out = new EnumMap<>(Status.class);
        findings.stream().filter(f -> f.context().equals(context)).forEach(f -> out.merge(f.status, 1, Integer::sum));
        return out;
    }

    /** Distinct problems in a context, not counting review hints. */
    public long problems(String context) {
        return findings.stream().filter(f -> f.context().equals(context) && f.status != Status.HEURISTIC_CLASS).count();
    }

    /** Distinct runtime problems: the number that has to reach 0 for the mod to load. */
    public long blocking() {
        return problems("runtime");
    }

    private List<Finding> sorted(String context) {
        return findings.stream().filter(f -> f.context().equals(context))
                .sorted(Comparator.comparing((Finding f) -> f.status.ordinal()).thenComparing(f -> -f.count).thenComparing(f -> f.symbol))
                .toList();
    }

    public JsonObject toJson() {
        JsonObject out = new JsonObject();
        out.addProperty("schema", "rose-analyze/1");
        out.addProperty("jar", jar.toString());
        out.addProperty("modId", modId);
        out.addProperty("source", source);
        out.addProperty("target", "26.3");
        out.addProperty("classes", classes);
        out.addProperty("references", references);
        out.addProperty("redirected", redirected);
        out.addProperty("adapted", adapted);
        out.addProperty("bridged", bridged);
        JsonObject problems = new JsonObject();
        CONTEXTS.forEach(c -> problems.addProperty(c, problems(c)));
        out.add("problems", problems);
        JsonObject counts = new JsonObject();
        countByStatus("runtime").forEach((s, n) -> counts.addProperty(s.name(), n));
        out.add("runtimeByStatus", counts);
        JsonArray list = new JsonArray();
        for (String context : CONTEXTS) {
            for (Finding f : sorted(context)) {
                JsonObject j = new JsonObject();
                j.addProperty("status", f.status.name());
                j.addProperty("work", f.status.work);
                j.addProperty("context", context);
                j.addProperty("kind", f.kind);
                j.addProperty("symbol", f.symbol);
                j.addProperty("readable", f.readable);
                j.addProperty("target", f.target);
                j.addProperty("count", f.count);
                j.add("usedIn", Analyzer.strings(f.usedIn));
                list.add(j);
            }
        }
        out.add("findings", list);
        JsonObject forge = new JsonObject();
        forgeSurface.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> forge.addProperty(e.getKey(), e.getValue()));
        out.add("forgeSurface", forge);
        out.add("nestedJars", Analyzer.strings(nestedJars));
        out.add("dataGenerationClasses", Analyzer.strings(new TreeSet<>(contexts.dataGeneration())));
        out.add("integrationClasses", Analyzer.strings(new TreeSet<>(contexts.integration())));
        return out;
    }

    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        md.append("# Rose compatibility report: ").append(modId != null ? modId : jar.getFileName()).append("\n\n");
        md.append("| | |\n|---|---|\n");
        md.append("| Jar | `").append(jar.getFileName()).append("` |\n");
        md.append("| Source | ").append(source).append(" → Minecraft 26.3 |\n");
        md.append("| Classes | ").append(classes).append(" (").append(contexts.integration().size())
                .append(" optional-integration, ").append(contexts.dataGeneration().size()).append(" data-generation) |\n");
        md.append("| References checked | ").append(references).append(" |\n");
        md.append("| Calls redirected to era-bridge shims | ").append(redirected).append(" |\n");
        md.append("| Calls adapted to a widened signature | ").append(adapted).append(" (automatic, see CallAdapter) |\n");
        md.append("| Overrides bridged to their 26.3 method | ").append(bridged).append(" (bridges.tsv, see InheritanceBridger) |\n");
        md.append("| **Runtime problems** | **").append(problems("runtime")).append("** (must reach 0 to load) |\n");
        md.append("| Integration problems | ").append(problems("integration")).append(" (only with JEI/EMI/... installed) |\n");
        md.append("| Data-generation problems | ").append(problems("data-generation")).append(" (build time only) |\n");
        if (!nestedJars.isEmpty()) md.append("| Nested jars | ").append(String.join(", ", nestedJars)).append(" |\n");
        md.append("\nEverything not listed resolved automatically, including pure renames between versions. ")
                .append("Members whose signature mentions an unresolved class are not repeated; fix the class first.\n\n");

        md.append("## Runtime summary\n\n| Status | Work | Count | Meaning |\n|---|---|---:|---|\n");
        countByStatus("runtime").forEach((s, n) -> md.append("| ").append(s.name()).append(" | ").append(s.work).append(" | ")
                .append(n).append(" | ").append(s.meaning).append(" |\n"));
        appendFindings(md, "runtime", "## ");

        md.append("\n# Optional integrations\n\nOnly matter when the other mod is installed.\n");
        appendFindings(md, "integration", "## Integration: ");

        md.append("\n# Data generators\n\nBuild-time only; they don't affect playing.\n\n| Status | Count |\n|---|---:|\n");
        countByStatus("data-generation").forEach((s, n) -> md.append("| ").append(s.name()).append(" | ").append(n).append(" |\n"));
        return md.toString();
    }

    private void appendFindings(StringBuilder md, String context, String headingPrefix) {
        Status current = null;
        for (Finding f : sorted(context)) {
            if (f.status != current) {
                current = f.status;
                md.append("\n").append(headingPrefix).append(current.name()).append("\n\n").append(current.meaning).append(".\n\n");
                md.append("| Symbol (readable 1.20.1 name) | Kind | Uses | Used in |\n|---|---|---:|---|\n");
            }
            md.append("| `").append(f.readable.equals(f.symbol) ? f.symbol : f.readable).append("`");
            if (!f.target.equals("-") && f.status != Status.FORGE_API) md.append(" → `").append(f.target).append("`");
            md.append(" | ").append(f.kind).append(" | ").append(f.count).append(" | ");
            md.append(String.join(", ", f.usedIn.stream().limit(USED_IN_LIMIT).map(Report::shortName).toList()));
            if (f.usedIn.size() > USED_IN_LIMIT) md.append(", +").append(f.usedIn.size() - USED_IN_LIMIT);
            md.append(" |\n");
        }
    }

    public void write(Path json, Path markdown) throws IOException {
        Files.createDirectories(json.toAbsolutePath().getParent());
        Files.writeString(json, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(toJson()));
        Files.writeString(markdown, toMarkdown());
    }

    private static String shortName(String internalName) {
        return internalName.substring(internalName.lastIndexOf('/') + 1);
    }
}

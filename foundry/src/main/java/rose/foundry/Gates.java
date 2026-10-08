package rose.foundry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The checks a fix must pass before it lands. They run in the task's worktree and are decided here, never by the
 * agent:
 * <ol>
 *   <li><b>scope</b>: only the files this kind of task may change were changed (no editing tests or build files);</li>
 *   <li><b>build</b>: {@code ./gradlew build} (compiles everything, runs unit tests);</li>
 *   <li><b>analyze</b>: the task's call is now resolved, and no runtime finding exists that wasn't there before;</li>
 *   <li><b>gametests</b>: the in-game GameTests still pass.</li>
 * </ol>
 */
final class Gates {
    record Outcome(boolean passed, String gate, String detail) {}

    private final Path logs;

    Gates(Path logs) {
        this.logs = logs;
    }

    Outcome check(Task task, Path worktree, List<String> changedFiles, Path baselineReport) throws IOException, InterruptedException {
        Outcome scope = scope(task, changedFiles);
        if (!scope.passed()) return scope;

        Proc.Result build = Proc.run(worktree, Proc.gradle(worktree, "build", "--console=plain", "-q"), logs.resolve("gate-build.log"), 30);
        if (!build.ok()) return new Outcome(false, "build", build.timedOut() ? "timed out" : build.tail(40));

        Path jar = Path.of(task.get("jar")).toAbsolutePath();
        if (!jar.isAbsolute() || !Files.exists(jar)) jar = worktree.resolve(task.get("jar"));
        Proc.Result analyze = Proc.run(worktree, Proc.gradle(worktree, "-q", ":analyzer:run", "--args=" + jar),
                logs.resolve("gate-analyze.log"), 30);
        Path report = worktree.resolve("build/analyze").resolve(jar.getFileName().toString().replaceAll("\\.jar$", "") + ".rose.json");
        if (!analyze.ok() || !Files.exists(report)) return new Outcome(false, "analyze", "analyzer failed:\n" + analyze.tail(30));
        Outcome delta = compare(task, report, baselineReport);
        if (!delta.passed()) return delta;

        Proc.Result tests = Proc.run(worktree, Proc.gradle(worktree, ":boot:runGameTests", "--console=plain"),
                logs.resolve("gate-gametests.log"), 30);
        if (!tests.ok()) return new Outcome(false, "gametests", tests.timedOut() ? "timed out" : tests.tail(40));

        return new Outcome(true, "all", delta.detail());
    }

    /** Which files a task kind may touch. */
    static Outcome scope(Task task, List<String> changedFiles) {
        List<String> outside = new ArrayList<>();
        for (String f : changedFiles) {
            boolean allowed = switch (task.kind()) {
                case "redirect" -> (f.startsWith("eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/") && f.endsWith(".java"))
                        || f.equals("rosetta/rules/forge-1.20.1/redirects.tsv");
                default -> false;
            };
            if (!allowed) outside.add(f);
        }
        if (changedFiles.isEmpty()) return new Outcome(false, "scope", "no files were changed");
        if (!outside.isEmpty()) {
            return new Outcome(false, "scope", "changed files outside this task's scope (revert them): " + outside);
        }
        return new Outcome(true, "scope", "ok");
    }

    /** The task's symbol must be gone, and no runtime finding may appear that the baseline didn't have. */
    static Outcome compare(Task task, Path report, Path baseline) throws IOException {
        Set<String> now = runtimeKeys(report);
        Set<String> before = Files.exists(baseline) ? runtimeKeys(baseline) : Set.of();
        String symbol = task.get("symbol");
        List<String> stillThere = now.stream().filter(k -> k.endsWith("|" + symbol)).toList();
        if (!stillThere.isEmpty()) {
            return new Outcome(false, "analyze", "the call is still unresolved: " + stillThere
                    + "\n(RULE_BROKEN means the rule exists but the shim's name or signature doesn't match the task)");
        }
        List<String> added = now.stream().filter(k -> !before.contains(k)).sorted().toList();
        if (!added.isEmpty()) {
            return new Outcome(false, "analyze", "new runtime problems appeared: " + added.subList(0, Math.min(10, added.size())));
        }
        long fixed = before.stream().filter(k -> !now.contains(k)).count();
        return new Outcome(true, "analyze", "resolved; runtime findings " + before.size() + " -> " + now.size() + " (" + fixed + " fixed)");
    }

    /** "STATUS|symbol" for every runtime finding in a report. */
    static Set<String> runtimeKeys(Path report) throws IOException {
        JsonObject json = JsonParser.parseString(Files.readString(report)).getAsJsonObject();
        Set<String> out = new HashSet<>();
        for (JsonElement e : json.getAsJsonArray("findings")) {
            JsonObject f = e.getAsJsonObject();
            if ("runtime".equals(f.get("context").getAsString()) && !"HEURISTIC_CLASS".equals(f.get("status").getAsString())) {
                out.add(f.get("status").getAsString() + "|" + f.get("symbol").getAsString());
            }
        }
        return out;
    }
}

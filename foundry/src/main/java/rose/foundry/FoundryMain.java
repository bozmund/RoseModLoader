package rose.foundry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import rose.rosetta.NameLayer;
import rose.rosetta.RosettaMain;

/**
 * {@code rose foundry <command>}:
 * <pre>
 * plan &lt;report.rose.json&gt; &lt;mod.jar&gt;   create tasks from an analyzer report (and snapshot it as the baseline)
 * run [--max N] [--attempts N] [--task ID] [--agent pi|script:CMD] [--provider P] [--model M] [--thinking L]
 * status                                regenerate and print foundry/status.md
 * show ID                               print a task
 * pack ID                               print the context pack an agent would get (without running anything)
 * retry ID | reject ID REASON
 * </pre>
 */
public final class FoundryMain {
    public static void main(String[] argv) throws Exception {
        List<String> args = new ArrayList<>(List.of(argv));
        if (args.isEmpty()) {
            System.out.println("usage: rose foundry plan|run|status|show|pack|retry|reject ...");
            return;
        }
        Path home = Path.of("").toAbsolutePath();
        String command = args.removeFirst();
        switch (command) {
            case "plan" -> plan(home, Path.of(args.get(0)), Path.of(args.get(1)));
            case "run" -> run(home, args);
            case "status" -> {
                StatusPage.write(home, new TaskStore(home.resolve("foundry/tasks")));
                System.out.println(Files.readString(home.resolve("foundry/status.md")));
            }
            case "show" -> System.out.println(new TaskStore(home.resolve("foundry/tasks")).find(args.getFirst())
                    .map(Task::serialize).orElse("no task " + args.getFirst()));
            case "pack" -> {
                Task task = new TaskStore(home.resolve("foundry/tasks")).find(args.getFirst())
                        .orElseThrow(() -> new IllegalArgumentException("no task " + args.getFirst()));
                System.out.println(ContextPack.build(task, home, null));
            }
            case "retry" -> new Foundry(home, null).retry(args.getFirst());
            case "reject" -> new Foundry(home, null).reject(args.get(0), String.join(" ", args.subList(1, args.size())));
            default -> throw new IllegalArgumentException("unknown foundry command " + command);
        }
    }

    private static void plan(Path home, Path report, Path jar) throws Exception {
        NameLayer layer = NameLayer.read(RosettaMain.FORGE_1201_LAYER);
        TaskStore store = new TaskStore(home.resolve("foundry/tasks"));
        List<Task> created = new Planner(layer, home.resolve("corpus/minecraft/1.20.1/client-named.jar")).plan(report, jar, store);
        Path baseline = home.resolve("foundry/work/baseline").resolve(report.getFileName());
        Files.createDirectories(baseline.getParent());
        Files.copy(report, baseline, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[foundry] decompiling " + jar.getFileName() + " with readable names for context packs...");
        ModSources.prepare(jar, layer);
        StatusPage.write(home, store);
        System.out.println("[foundry] " + created.size() + " new task(s); baseline " + baseline);
    }

    private static void run(Path home, List<String> args) throws Exception {
        int max = Integer.parseInt(option(args, "--max", "1"));
        int attempts = Integer.parseInt(option(args, "--attempts", "2"));
        String only = option(args, "--task", null);
        String agentSpec = option(args, "--agent", "pi");
        Agent agent = agentSpec.startsWith("script:")
                ? new Agent.Script(agentSpec.substring("script:".length()))
                : new Agent.Pi(option(args, "--provider", "mony"), option(args, "--model", null),
                        option(args, "--thinking", "high"), Long.parseLong(option(args, "--timeout", "40")));
        new Foundry(home, agent).run(max, attempts, only);
        System.out.println(Files.readString(home.resolve("foundry/status.md")));
    }

    private static String option(List<String> args, String name, String fallback) {
        int i = args.indexOf(name);
        if (i < 0) return fallback;
        String value = args.get(i + 1);
        args.remove(i + 1);
        args.remove(i);
        return value;
    }

    private FoundryMain() {}
}

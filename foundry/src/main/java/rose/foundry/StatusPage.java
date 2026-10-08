package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** {@code foundry/status.md}: what the foundry has done and what is left, regenerated after every task. */
final class StatusPage {
    static void write(Path home, TaskStore store) throws IOException {
        List<Task> all = store.all();
        Map<Task.State, Integer> counts = new EnumMap<>(Task.State.class);
        for (Task.State s : Task.State.values()) counts.put(s, 0);
        all.forEach(t -> counts.merge(t.state(), 1, Integer::sum));
        int attempts = all.stream().mapToInt(t -> t.getInt("attempts", 0)).sum();
        long landedFirstTry = all.stream().filter(t -> t.state() == Task.State.LANDED && t.getInt("attempts", 0) == 1).count();

        StringBuilder md = new StringBuilder("# Foundry status\n\n_Updated ").append(Foundry.now()).append("_\n\n");
        md.append("| State | Tasks |\n|---|---:|\n");
        counts.forEach((s, n) -> md.append("| ").append(s.name().toLowerCase()).append(" | ").append(n).append(" |\n"));
        md.append("\nAttempts so far: ").append(attempts).append(". Landed on the first attempt: ").append(landedFirstTry).append(".\n");

        appendList(md, "Landed on `develop` (review before merging to `main`)", store.list(Task.State.LANDED), "landed");
        appendList(md, "Escalated (needs a stronger model or a human)", store.list(Task.State.ESCALATED), "escalated");
        appendList(md, "In progress", store.list(Task.State.CLAIMED), "attempts");

        List<Task> open = store.openByImpact();
        md.append("\n## Open (").append(open.size()).append("), most used first\n\n");
        open.stream().limit(25).forEach(t -> md.append("- `").append(t.id()).append("` ").append(t.get("readable"))
                .append(" - ").append(t.get("uses")).append(" uses\n"));
        if (open.size() > 25) md.append("- ... and ").append(open.size() - 25).append(" more\n");

        Files.writeString(home.resolve("foundry/status.md"), md.toString());
    }

    private static void appendList(StringBuilder md, String title, List<Task> tasks, String field) {
        if (tasks.isEmpty()) return;
        md.append("\n## ").append(title).append("\n\n");
        tasks.forEach(t -> md.append("- `").append(t.id()).append("` ").append(t.get("readable"))
                .append(" (").append(field).append(": ").append(t.get(field)).append(")\n"));
    }

    private StatusPage() {}
}

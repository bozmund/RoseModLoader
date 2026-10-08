package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The work loop. For each open task, most impactful first:
 * <ol>
 *   <li>claim it and open its worktree from the latest {@code develop};</li>
 *   <li>write the context pack and run the agent;</li>
 *   <li>run the gates; on success commit and land on {@code develop} (fast-forward), then close the worktree;</li>
 *   <li>on failure, feed the gate output back and retry; after {@link #MAX_ATTEMPTS} attempts write an escalation
 *       ticket for a stronger model or a human.</li>
 * </ol>
 * Tasks run one at a time, so each starts from everything landed before it.
 */
public final class Foundry {
    static final int MAX_ATTEMPTS = 5;
    /** After this many failures the worktree is reset so the next attempt starts clean. */
    static final int RESET_AFTER = 3;

    private final Path home;
    private final TaskStore store;
    private final Worktrees worktrees;
    private final Agent agent;

    public Foundry(Path home, Agent agent) {
        this.home = home;
        this.store = new TaskStore(home.resolve("foundry/tasks"));
        this.worktrees = new Worktrees(home);
        this.agent = agent;
    }

    TaskStore store() {
        return store;
    }

    /** Works through up to {@code maxTasks} open tasks, giving each up to {@code attemptsPerRun} attempts. */
    public void run(int maxTasks, int attemptsPerRun, String onlyId) throws Exception {
        worktrees.ensureIntegrationBranch();
        List<Task> queue = onlyId != null ? store.find(onlyId).stream().toList() : store.openByImpact();
        int done = 0;
        for (Task task : queue) {
            if (done++ >= maxTasks) break;
            if (task.state() != Task.State.OPEN && task.state() != Task.State.CLAIMED) {
                log("skip " + task.id() + " (" + task.state() + ")");
                continue;
            }
            work(task, attemptsPerRun);
            StatusPage.write(home, store);
        }
        StatusPage.write(home, store);
    }

    private void work(Task task, int attemptsPerRun) throws Exception {
        log("task " + task.id() + ": " + task.get("readable") + " (" + task.get("uses") + " uses) with " + agent.describe());
        store.move(task, Task.State.CLAIMED);
        Path worktree = worktrees.open(task.id());
        Path logs = home.resolve("foundry/logs").resolve(task.id());
        Path baseline = home.resolve("foundry/work/baseline").resolve(Path.of(task.get("jar")).getFileName().toString().replaceAll("\\.jar$", "") + ".rose.json");

        String feedback = Files.exists(feedbackFile(task)) ? Files.readString(feedbackFile(task)) : null;
        for (int run = 0; run < attemptsPerRun; run++) {
            int attempt = task.getInt("attempts", 0) + 1;
            task.set("attempts", attempt);
            store.save(task);
            if (attempt > 1 && (attempt - 1) % RESET_AFTER == 0) worktrees.reset(task.id());

            Path taskFile = worktree.resolve(".foundry").resolve("TASK.md");
            Files.createDirectories(taskFile.getParent());
            Files.writeString(taskFile, ContextPack.build(task, worktree, feedback));
            Path attemptLogs = logs.resolve("attempt-" + attempt);

            String summary = agent.run(task, worktree, taskFile, attemptLogs.resolve("agent.log"));
            Gates.Outcome outcome = new Gates(attemptLogs).check(task, worktree, worktrees.changedFiles(task.id()), baseline);
            log("  attempt " + attempt + ": " + (outcome.passed() ? "PASSED" : "failed at " + outcome.gate()) + " - " + firstLine(outcome.detail()));

            if (outcome.passed()) {
                worktrees.commit(task.id(), "foundry: redirect " + task.get("readable") + "\n\n" + task.id() + " (" + agent.describe()
                        + ", attempt " + attempt + ")\n" + outcome.detail());
                worktrees.land(task.id());
                worktrees.close(task.id(), true);
                task.set("landed", now());
                task.set("result", outcome.detail());
                task.appendBody("\n## Landed " + now() + "\n\nAttempt " + attempt + " by " + agent.describe() + ". " + outcome.detail()
                        + "\n\nAgent summary:\n\n> " + summary.replace("\n", "\n> ") + "\n");
                store.move(task, Task.State.LANDED);
                return;
            }

            feedback = outcome.gate() + ": " + outcome.detail();
            Files.createDirectories(feedbackFile(task).getParent());
            Files.writeString(feedbackFile(task), feedback);
            task.set("lastFeedback", outcome.gate());
            store.save(task);
            if (attempt >= MAX_ATTEMPTS) {
                escalate(task, outcome, summary);
                return;
            }
        }
        // Out of attempts for this run: back in the queue, worktree kept so the next run continues from it.
        store.move(task, Task.State.OPEN);
    }

    private void escalate(Task task, Gates.Outcome outcome, String summary) throws IOException, InterruptedException {
        task.set("escalated", now());
        task.appendBody("""

                ## Escalated %s

                %d attempts by %s failed. Last gate: **%s**

                ```
                %s
                ```

                Last agent message:

                > %s

                Next step: a stronger model or a human takes this task. The worktree `foundry/worktrees/%s` (branch `%s`)
                holds the last attempt. Run `rose foundry retry %s` to put it back in the queue after fixing the cause.
                """.formatted(now(), task.getInt("attempts", 0), agent.describe(), outcome.gate(), outcome.detail(),
                summary.replace("\n", "\n> "), task.id(), worktrees.branch(task.id()), task.id()));
        store.move(task, Task.State.ESCALATED);
        log("  escalated " + task.id());
    }

    /** Puts an escalated or rejected task back in the queue (attempt counter reset). */
    public void retry(String id) throws Exception {
        Task task = store.find(id).orElseThrow(() -> new IllegalArgumentException("no task " + id));
        task.set("attempts", 0);
        store.move(task, Task.State.OPEN);
    }

    /** Gives up on a task: closes its worktree and branch, keeps the task file as a record. */
    public void reject(String id, String reason) throws Exception {
        Task task = store.find(id).orElseThrow(() -> new IllegalArgumentException("no task " + id));
        worktrees.close(id, true);
        task.appendBody("\n## Rejected " + now() + "\n\n" + reason + "\n");
        store.move(task, Task.State.REJECTED);
    }

    private Path feedbackFile(Task task) {
        return home.resolve("foundry/work/feedback").resolve(task.id() + ".txt");
    }

    private static String firstLine(String text) {
        String line = text.lines().findFirst().orElse("");
        return line.length() > 160 ? line.substring(0, 160) + "..." : line;
    }

    static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    static void log(String message) {
        System.out.println("[foundry] " + message);
    }
}

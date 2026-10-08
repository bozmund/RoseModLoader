package rose.foundry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Something that edits a worktree to complete a task: the pi coding agent, or a script (for testing the foundry). */
interface Agent {
    /**
     * @param taskFile the context pack, inside the worktree
     * @return the agent's final message (for reports), never null
     */
    String run(Task task, Path worktree, Path taskFile, Path log) throws IOException, InterruptedException;

    String describe();

    /** {@code pi} in print mode with JSON events, using the configured provider/model (e.g. the local "mony" model). */
    record Pi(String provider, String model, String thinking, long timeoutMinutes) implements Agent {
        @Override
        public String run(Task task, Path worktree, Path taskFile, Path log) throws IOException, InterruptedException {
            List<String> command = new ArrayList<>(List.of(Proc.WINDOWS ? "pi.cmd" : "pi", "-p", "--mode", "json",
                    "--no-session", "--approve", "--no-extensions", "--no-skills"));
            if (provider != null) command.addAll(List.of("--provider", provider));
            if (model != null) command.addAll(List.of("--model", model));
            if (thinking != null) command.addAll(List.of("--thinking", thinking));
            command.add("@" + worktree.relativize(taskFile).toString().replace('\\', '/'));
            command.add("Complete the Rose foundry task described above. Edit only the files it names, check with the "
                    + "command it gives, then reply with a short summary.");
            Proc.Result result = Proc.run(worktree, command, log, timeoutMinutes);
            String last = lastAssistantText(log);
            if (result.timedOut()) return "(agent timed out after " + timeoutMinutes + " min) " + last;
            if (result.exit() != 0) return "(agent exited with " + result.exit() + ") " + result.tail(15);
            return last;
        }

        @Override
        public String describe() {
            return "pi " + (provider != null ? provider + "/" : "") + (model != null ? model : "(default model)");
        }

        /** The final assistant message from pi's JSON event stream. */
        private static String lastAssistantText(Path log) throws IOException {
            if (!Files.exists(log)) return "";
            String last = "";
            for (String line : Files.readAllLines(log)) {
                if (!line.startsWith("{")) continue;
                try {
                    JsonObject event = JsonParser.parseString(line).getAsJsonObject();
                    String text = findText(event);
                    if (text != null && !text.isBlank()) last = text;
                } catch (RuntimeException ignored) {
                    // not an event line
                }
            }
            return last.length() > 4000 ? last.substring(last.length() - 4000) : last;
        }

        private static String findText(JsonObject event) {
            if (!"message_end".equals(str(event, "type")) && !"agent_end".equals(str(event, "type"))) return null;
            JsonObject message = event.has("message") && event.get("message").isJsonObject() ? event.getAsJsonObject("message") : null;
            if (message == null || !"assistant".equals(str(message, "role")) || !message.has("content")) return null;
            StringBuilder out = new StringBuilder();
            for (var part : message.getAsJsonArray("content")) {
                JsonObject p = part.getAsJsonObject();
                if ("text".equals(str(p, "type"))) out.append(str(p, "text"));
            }
            return out.toString();
        }

        private static String str(JsonObject o, String key) {
            return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
        }
    }

    /**
     * Runs a shell command in the worktree with {@code TASK_ID}, {@code TASK_FILE}, {@code SHIM_FILE},
     * {@code SHIM_SIGNATURE} and {@code RULE_LINE} in the environment. Used to test the foundry itself.
     */
    record Script(String command) implements Agent {
        @Override
        public String run(Task task, Path worktree, Path taskFile, Path log) throws IOException, InterruptedException {
            List<String> shell = Proc.WINDOWS && !command.endsWith(".sh") ? List.of("cmd", "/c", command) : List.of(bash(), "-c", command);
            Proc.Result r = Proc.run(worktree, shell, log, 10, Map.of(
                    "TASK_ID", task.id(), "TASK_FILE", taskFile.toString(),
                    "SHIM_FILE", task.get("shimFile"), "SHIM_SIGNATURE", task.get("shimSignature"),
                    "RULE_LINE", task.get("ruleLine")));
            return "script exited with " + r.exit() + ": " + r.tail(5);
        }

        @Override
        public String describe() {
            return "script: " + command;
        }

        /**
         * On Windows, "bash" on the PATH is often WSL's launcher, not Git Bash. Use $ROSE_BASH, or the bash that ships
         * next to git.
         */
        static String bash() throws IOException, InterruptedException {
            String configured = System.getenv("ROSE_BASH");
            if (configured != null && !configured.isBlank()) return configured;
            if (!Proc.WINDOWS) return "bash";
            Process where = new ProcessBuilder("where", "git").redirectErrorStream(true).start();
            String out = new String(where.getInputStream().readAllBytes()).trim();
            where.waitFor();
            for (String line : out.lines().toList()) {
                Path gitRoot = Path.of(line.trim()).getParent().getParent(); // ...\Git\cmd\git.exe -> ...\Git
                Path candidate = gitRoot.resolve("bin").resolve("bash.exe");
                if (Files.exists(candidate)) return candidate.toString();
            }
            throw new IOException("Git Bash not found; set ROSE_BASH to a bash.exe");
        }
    }
}

package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Runs a child process with a time limit, logging its output to a file. */
final class Proc {
    static final boolean WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    record Result(int exit, boolean timedOut, Path log) {
        boolean ok() {
            return exit == 0 && !timedOut;
        }

        /** The last lines of the log, for feedback and reports. */
        String tail(int lines) throws IOException {
            if (!Files.exists(log)) return "";
            List<String> all = Files.readAllLines(log, java.nio.charset.StandardCharsets.UTF_8);
            return String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
        }
    }

    static Result run(Path dir, List<String> command, Path log, long timeoutMinutes, Map<String, String> env)
            throws IOException, InterruptedException {
        Files.createDirectories(log.getParent());
        ProcessBuilder pb = new ProcessBuilder(command).directory(dir.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile())
                .redirectInput(ProcessBuilder.Redirect.from(new java.io.File(WINDOWS ? "NUL" : "/dev/null")));
        pb.environment().putAll(env);
        Process process = pb.start();
        if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            return new Result(-1, true, log);
        }
        return new Result(process.exitValue(), false, log);
    }

    static Result run(Path dir, List<String> command, Path log, long timeoutMinutes) throws IOException, InterruptedException {
        return run(dir, command, log, timeoutMinutes, Map.of());
    }

    /** {@code gradlew} (or {@code gradlew.bat}) of a checkout, followed by arguments. */
    static List<String> gradle(Path checkout, String... args) {
        List<String> command = new ArrayList<>();
        command.add(checkout.resolve(WINDOWS ? "gradlew.bat" : "gradlew").toString());
        command.addAll(List.of(args));
        return command;
    }

    /** Runs git and returns its trimmed output; throws if it fails. */
    static String git(Path dir, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of("git"));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).directory(dir.toFile()).redirectErrorStream(true).start();
        String out = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
        if (process.waitFor() != 0) throw new IOException("git " + String.join(" ", args) + " failed: " + out);
        return out;
    }

    private Proc() {}
}

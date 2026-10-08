package rose.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Starts the game (through Gradle) with the Agent Bridge on, and waits until the bridge answers. */
final class Launcher {
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    static String taskFor(String target) {
        return switch (target) {
            case "client" -> ":boot:runClient";
            case "server" -> ":boot:runServer";
            case "gametest" -> ":boot:runGameTests";
            default -> throw new IllegalArgumentException("target must be client, server or gametest");
        };
    }

    /**
     * @return the bridge once it answers, or {@code null} if {@code waitSeconds} is 0
     */
    static Bridge launch(Path home, String target, int waitSeconds, List<String> gradleArgs) throws Exception {
        Path discovery = Bridge.discoveryFile(home, target);
        if (Files.exists(discovery)) {
            try {
                Bridge existing = Bridge.connect(home, target);
                if (existing.alive()) throw new IllegalStateException(target + " is already running (rose stop " + target + " first)");
            } catch (IOException ignored) {
                // stale file from a crashed game
            }
            Files.deleteIfExists(discovery);
        }

        Path log = home.resolve("run").resolve(target).resolve("rose-launch.log");
        Files.createDirectories(log.getParent());
        List<String> command = new ArrayList<>();
        command.add(home.resolve(WINDOWS ? "gradlew.bat" : "gradlew").toString());
        command.add(taskFor(target));
        command.add("--console=plain");
        command.addAll(gradleArgs);
        Process process = new ProcessBuilder(command)
                .directory(home.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .redirectInput(ProcessBuilder.Redirect.from(WINDOWS ? new java.io.File("NUL") : new java.io.File("/dev/null")))
                .start();
        System.err.println("[rose] launching " + target + " (pid " + process.pid() + "), log: " + log);
        if (waitSeconds <= 0) return null;

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(waitSeconds);
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                throw new IllegalStateException(target + " exited with code " + process.exitValue() + " before the bridge came up; see " + log);
            }
            if (Files.exists(discovery)) {
                try {
                    Bridge bridge = Bridge.connect(home, target);
                    if (bridge.alive()) return bridge;
                } catch (IOException ignored) {
                    // file written but server not answering yet
                }
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("timed out after " + waitSeconds + "s waiting for the " + target + " bridge; see " + log);
    }

    private Launcher() {}
}

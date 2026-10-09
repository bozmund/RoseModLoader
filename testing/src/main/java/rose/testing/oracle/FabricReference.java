package rose.testing.oracle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rose.boot.mojang.Downloader;

/**
 * Runs the answer key: a headless Fabric 26.3 server with the mods' native ports and the oracle dumper, which writes
 * its dump and stops the server. Fabric API's GameTest mode starts vanilla's GameTest server, so no EULA is needed.
 *
 * <p>{@code FabricReference <run-dir> <dump-dir> <namespaces> <server-launcher-url> <mod.jar>...}
 */
public final class FabricReference {
    private static final long TIMEOUT_MINUTES = 15;

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("usage: FabricReference <run-dir> <dump-dir> <namespaces> <server-launcher-url> <mod.jar>...");
            System.exit(2);
        }
        Path runDir = Path.of(args[0]).toAbsolutePath();
        Path dumpDir = Path.of(args[1]).toAbsolutePath();
        String namespaces = args[2];
        String launcherUrl = args[3];

        Path mods = runDir.resolve("mods");
        Files.createDirectories(mods);
        try (var old = Files.list(mods)) {
            for (Path p : old.toList()) Files.delete(p);
        }
        for (int i = 4; i < args.length; i++) {
            Path jar = Path.of(args[i]);
            if (!Files.exists(jar)) throw new IllegalStateException("missing " + jar + " (run ./gradlew corpusInputs)");
            Files.copy(jar, mods.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        Path launcher = runDir.resolve("fabric-server-launch.jar");
        new Downloader().download(launcherUrl, launcher, null);

        deleteRecursively(dumpDir);
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>(List.of(java, "-Xmx3G",
                "-Dfabric-api.gametest",
                "-Dfabric-api.gametest.report-file=" + runDir.resolve("gametest-report.xml"),
                "-Drose.oracle.out=" + dumpDir,
                "-Drose.oracle.namespaces=" + namespaces,
                "-jar", launcher.toString(), "nogui"));
        System.out.println("[oracle] starting the Fabric reference server in " + runDir);
        Process process = new ProcessBuilder(command).directory(runDir.toFile()).inheritIO().start();
        if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new IllegalStateException("reference server still running after " + TIMEOUT_MINUTES + " minutes");
        }
        if (!Files.exists(dumpDir.resolve("registries.json"))) {
            throw new IllegalStateException("reference server exited (code " + process.exitValue() + ") without writing a dump to " + dumpDir);
        }
        System.out.println("[oracle] reference dump: " + dumpDir);
    }

    private static void deleteRecursively(Path dir) throws java.io.IOException {
        if (!Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(p);
        }
    }

    private FabricReference() {}
}

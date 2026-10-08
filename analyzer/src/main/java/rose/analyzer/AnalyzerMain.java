package rose.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import rose.rosetta.NameLayer;
import rose.rosetta.RosettaMain;

/**
 * {@code rose analyze <mod.jar> [--out DIR]}: writes {@code <jar>.rose.json} and {@code <jar>.rose.md} (default:
 * {@code build/analyze/}) and prints a one-screen summary. Exit code 0 = no blocking problems, 3 = problems found.
 */
public final class AnalyzerMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: analyze <mod.jar> [--out DIR] [--minecraft-classpath FILE]");
            System.exit(2);
        }
        Path jar = Path.of(args[0]).toAbsolutePath();
        Path out = Path.of("build", "analyze");
        Path classpathFile = Path.of("build", "minecraft", "classpath-26.3.txt");
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--out" -> out = Path.of(args[++i]);
                case "--minecraft-classpath" -> classpathFile = Path.of(args[++i]);
                default -> throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }
        require(RosettaMain.FORGE_1201_LAYER, "run ./gradlew :rosetta:buildNameLayers");
        require(classpathFile, "run ./gradlew :boot:installMinecraft");
        Path vanillaOld = Path.of("corpus", "minecraft", "1.20.1", "client-named.jar");
        require(vanillaOld, "run ./gradlew corpusSetup");

        NameLayer layer = NameLayer.read(RosettaMain.FORGE_1201_LAYER);
        List<Path> targetJars = new ArrayList<>();
        for (String line : Files.readAllLines(classpathFile)) if (!line.isBlank()) targetJars.add(Path.of(line));
        ClassIndex target = ClassIndex.of(targetJars, true);
        ClassIndex old = ClassIndex.of(List.of(vanillaOld), true);

        Report report = new Analyzer(layer, target, old).analyze(jar);
        String base = jar.getFileName().toString().replaceAll("\\.jar$", "");
        Path json = out.resolve(base + ".rose.json");
        Path md = out.resolve(base + ".rose.md");
        report.write(json, md);

        System.out.println("Rose analyze: " + (report.modId() != null ? report.modId() : base)
                + " (" + report.source() + " -> 26.3), " + report.classes() + " classes, " + report.references() + " references");
        report.countByStatus("runtime").forEach((s, n) -> System.out.printf("  %-27s %5d  [%s]%n", s, n, s.work));
        System.out.println("  runtime problems: " + report.blocking()
                + "   integration (optional mods): " + report.problems("integration")
                + "   data generators: " + report.problems("data-generation"));
        System.out.println("  report: " + md.toAbsolutePath());
        System.out.println("  json:   " + json.toAbsolutePath());
        System.exit(report.blocking() == 0 ? 0 : 3);
    }

    private static void require(Path file, String hint) {
        if (!Files.exists(file)) {
            System.err.println("missing " + file + " (" + hint + ")");
            System.exit(2);
        }
    }

    private AnalyzerMain() {}
}

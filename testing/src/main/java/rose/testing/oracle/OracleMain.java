package rose.testing.oracle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * {@code OracleMain <rose-dump> <reference-dump> <allow-list|-> <report-prefix> [--strict]}: writes
 * {@code <report-prefix>.md} and {@code .json}. With {@code --strict}, exits 1 when any difference is not
 * allow-listed.
 */
public final class OracleMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("usage: OracleMain <rose-dump> <reference-dump> <allow-list|-> <report-prefix> [--strict]");
            System.exit(2);
        }
        Path rose = Path.of(args[0]);
        Path reference = Path.of(args[1]);
        AllowList allow = args[2].equals("-") ? AllowList.empty() : AllowList.read(Path.of(args[2]));
        Path prefix = Path.of(args[3]);
        boolean strict = List.of(args).contains("--strict");

        List<Difference> differences = OracleDiff.compare(rose, reference);
        String title = prefix.getFileName().toString();
        OracleReport report = new OracleReport(title, differences, allow);
        if (prefix.getParent() != null) Files.createDirectories(prefix.getParent());
        Path md = prefix.resolveSibling(title + ".md");
        Files.writeString(md, report.markdown());
        Files.writeString(prefix.resolveSibling(title + ".json"), report.json());

        System.out.println("[oracle] " + report.open().size() + " open, " + (differences.size() - report.open().size())
                + " allowed. Report: " + md.toAbsolutePath());
        if (strict && !report.open().isEmpty()) System.exit(1);
    }

    private OracleMain() {}
}

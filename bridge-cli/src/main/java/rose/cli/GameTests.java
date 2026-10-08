package rose.cli;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Runs GameTests headless through Gradle and summarizes vanilla's JUnit-style report. */
final class GameTests {
    private static final Pattern TESTCASE =
            Pattern.compile("<testcase[^>]*name=\"([^\"]+)\"[^>]*?(?:/>|>(.*?)</testcase>)", Pattern.DOTALL);
    private static final Pattern FAILURE_MESSAGE = Pattern.compile("<failure[^>]*message=\"([^\"]*)\"");

    static int run(Path home, String selector) throws Exception {
        JsonObject summary = runForJson(home, selector);
        System.out.println(RoseCli.PRETTY.toJson(summary));
        return summary.get("failed").getAsInt() == 0 && summary.get("exitCode").getAsInt() == 0 ? 0 : 1;
    }

    /** {passed, failed, exitCode, tests:[{name, passed, message}], report, log} */
    static JsonObject runForJson(Path home, String selector) throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        Path log = home.resolve("run").resolve("gametest").resolve("rose-test.log");
        Files.createDirectories(log.getParent());
        Path report = home.resolve("build").resolve("gametest").resolve("report.xml");
        Files.deleteIfExists(report);

        List<String> command = new ArrayList<>(List.of(
                home.resolve(windows ? "gradlew.bat" : "gradlew").toString(), ":boot:runGameTests", "--console=plain"));
        if (selector != null) command.add("-Prose.tests=" + selector);
        int exit = new ProcessBuilder(command).directory(home.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start().waitFor();

        JsonArray tests = new JsonArray();
        int passed = 0;
        int failed = 0;
        if (Files.exists(report)) {
            Matcher m = TESTCASE.matcher(Files.readString(report));
            while (m.find()) {
                String body = m.group(2) != null ? m.group(2) : "";
                Matcher failure = FAILURE_MESSAGE.matcher(body);
                boolean ok = !body.contains("<failure");
                if (ok) passed++; else failed++;
                JsonObject test = new JsonObject();
                test.addProperty("name", m.group(1));
                test.addProperty("passed", ok);
                if (!ok) test.addProperty("message", failure.find() ? failure.group(1) : "failed");
                tests.add(test);
            }
        } else if (exit != 0) {
            failed = 1; // crashed before a report was written; see the log
        }

        JsonObject out = new JsonObject();
        out.addProperty("passed", passed);
        out.addProperty("failed", failed);
        out.addProperty("exitCode", exit);
        out.add("tests", tests);
        out.addProperty("report", report.toString());
        out.addProperty("log", log.toString());
        return out;
    }

    private GameTests() {}
}

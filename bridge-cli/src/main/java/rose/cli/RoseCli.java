package rose.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code rose} command.
 *
 * <pre>
 * rose launch client|server [--no-wait] [--wait SECONDS]   start the game with the Agent Bridge on
 * rose ctl [--target T] METHOD [key=value ...] [--params JSON]
 * rose methods [--target T]                                 what the running game can do
 * rose events [--target T] [--after N] [--types a,b] [--follow]
 * rose stop [client|server]
 * rose test [SELECTOR]                                      run GameTests headless (e.g. sample:*)
 * rose mcp                                                  MCP server on stdin/stdout (for Claude Code etc.)
 * </pre>
 *
 * Output is JSON on stdout; progress and errors go to stderr. Exit code 0 = success.
 */
public final class RoseCli {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

    public static void main(String[] args) {
        try {
            System.exit(run(new ArrayList<>(List.of(args))));
        } catch (Bridge.BridgeException e) {
            System.err.println("error " + e.code + ": " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            System.exit(1);
        }
    }

    static int run(List<String> args) throws Exception {
        if (args.isEmpty() || args.getFirst().equals("help") || args.getFirst().equals("--help")) {
            System.out.println(usage());
            return 0;
        }
        Path home = home();
        String command = args.removeFirst();
        String target = option(args, "--target");
        switch (command) {
            case "launch" -> {
                String side = args.isEmpty() ? "client" : args.removeFirst();
                int wait = args.remove("--no-wait") ? 0 : Integer.parseInt(orDefault(option(args, "--wait"), "300"));
                Bridge bridge = Launcher.launch(home, side, wait, args);
                if (bridge != null) print(bridge.call("rose.ping", new JsonObject()));
            }
            case "ctl" -> {
                if (args.isEmpty()) throw new IllegalArgumentException("usage: rose ctl [--target T] METHOD [key=value ...]");
                String method = args.removeFirst();
                print(Bridge.connect(home, target).call(method, params(args)));
            }
            case "methods" -> print(Bridge.connect(home, target).call("rose.methods", new JsonObject()));
            case "events" -> events(Bridge.connect(home, target), args);
            case "stop" -> {
                String side = args.isEmpty() ? target : args.removeFirst();
                Bridge bridge = Bridge.connect(home, side);
                System.err.println("[rose] stopping " + bridge.target());
                stop(home, bridge);
            }
            case "test" -> {
                return GameTests.run(home, args.isEmpty() ? null : args.getFirst());
            }
            case "foundry" -> {
                // Runs in the game's Java through Gradle (it builds and analyzes); arguments pass through.
                boolean windows = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win");
                List<String> gradle = new ArrayList<>(List.of(home.resolve(windows ? "gradlew.bat" : "gradlew").toString(),
                        "-q", "--console=plain", ":foundry:run", "--args=" + String.join(" ", args)));
                return new ProcessBuilder(gradle).directory(home.toFile()).inheritIO().start().waitFor();
            }
            case "analyze" -> {
                if (args.isEmpty()) throw new IllegalArgumentException("usage: rose analyze <mod.jar>");
                return analyze(home, Path.of(args.getFirst()).toAbsolutePath());
            }
            case "mcp" -> new McpServer(home).serve();
            default -> throw new IllegalArgumentException("unknown command '" + command + "'\n" + usage());
        }
        return 0;
    }

    /**
     * Runs the static analyzer through Gradle (it needs the game's Java and the corpus) and prints its summary.
     * Exit code: 0 = no runtime problems, 3 = problems found (see the report), 1 = the analyzer failed.
     */
    static int analyze(Path home, Path jar) throws Exception {
        if (!Files.exists(jar)) throw new IllegalArgumentException("no such file: " + jar);
        boolean windows = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win");
        List<String> command = List.of(home.resolve(windows ? "gradlew.bat" : "gradlew").toString(), "-q",
                ":analyzer:run", "--args=" + jar);
        int exit = new ProcessBuilder(command).directory(home.toFile()).inheritIO().start().waitFor();
        if (exit != 0) return 1;
        Path json = home.resolve("build/analyze").resolve(jar.getFileName().toString().replaceAll("\\.jar$", "") + ".rose.json");
        if (!Files.exists(json)) return 1;
        JsonObject report = JsonParser.parseString(Files.readString(json)).getAsJsonObject();
        return report.getAsJsonObject("problems").get("runtime").getAsLong() == 0 ? 0 : 3;
    }

    /** Asks the game to quit and waits until its bridge is gone, so a following launch doesn't collide with it. */
    static void stop(Path home, Bridge bridge) throws Exception {
        try {
            bridge.call(bridge.target().equals("client") ? "client.quit" : "server.stop", new JsonObject());
        } catch (java.io.IOException e) {
            // the connection may drop while the game shuts down
        }
        Path discovery = Bridge.discoveryFile(home, bridge.target());
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(90);
        while (System.nanoTime() < deadline) {
            if (!Files.exists(discovery) || !bridge.alive()) {
                Thread.sleep(1000); // let the JVM finish exiting
                return;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException(bridge.target() + " did not stop within 90s");
    }

    private static void events(Bridge bridge, List<String> args) throws Exception {
        long after = Long.parseLong(orDefault(option(args, "--after"), "0"));
        String types = option(args, "--types");
        boolean follow = args.remove("--follow");
        do {
            JsonObject params = new JsonObject();
            params.addProperty("after", after);
            if (types != null) {
                JsonArray list = new JsonArray();
                for (String t : types.split(",")) list.add(t.trim());
                params.add("types", list);
            }
            JsonObject result = bridge.call("events.poll", params).getAsJsonObject();
            for (JsonElement event : result.getAsJsonArray("events")) System.out.println(event);
            after = result.get("lastSeq").getAsLong();
            if (follow) Thread.sleep(500);
        } while (follow);
    }

    /** {@code key=value} pairs; values are parsed as JSON when possible (numbers, booleans, arrays), else strings. */
    static JsonObject params(List<String> args) {
        String json = option(args, "--params");
        JsonObject params = json != null ? JsonParser.parseString(json).getAsJsonObject() : new JsonObject();
        for (String arg : args) {
            int eq = arg.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("expected key=value, got '" + arg + "'");
            String value = arg.substring(eq + 1);
            JsonElement parsed;
            try {
                parsed = JsonParser.parseString(value);
                if (parsed.isJsonPrimitive() && parsed.getAsJsonPrimitive().isString() && !value.startsWith("\"")) {
                    parsed = new JsonPrimitive(value);
                }
            } catch (RuntimeException e) {
                parsed = new JsonPrimitive(value);
            }
            params.add(arg.substring(0, eq), parsed);
        }
        return params;
    }

    /** Rose's repository root: $ROSE_HOME, or the nearest folder above the working directory with gradlew + boot/. */
    static Path home() {
        String env = System.getenv("ROSE_HOME");
        if (env != null && !env.isBlank()) return Path.of(env).toAbsolutePath().normalize();
        for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            if (Files.exists(dir.resolve("settings.gradle.kts")) && Files.isDirectory(dir.resolve("boot"))) return dir;
        }
        throw new IllegalStateException("Can't find the Rose folder: run inside it or set ROSE_HOME");
    }

    static String option(List<String> args, String name) {
        int i = args.indexOf(name);
        if (i < 0) return null;
        if (i + 1 >= args.size()) throw new IllegalArgumentException(name + " needs a value");
        String value = args.get(i + 1);
        args.remove(i + 1);
        args.remove(i);
        return value;
    }

    private static String orDefault(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private static void print(JsonElement json) {
        System.out.println(PRETTY.toJson(json));
    }

    static String usage() {
        return """
                rose - control Rose Mod Loader games (Agent Bridge)

                  rose launch client|server [--wait SECONDS | --no-wait]  start the game with the bridge on
                  rose ctl [--target T] METHOD [key=value ...] [--params JSON]
                  rose methods [--target T]                                list what the running game can do
                  rose events [--target T] [--after N] [--types a,b] [--follow]
                  rose stop [client|server]
                  rose test [SELECTOR]                                     run GameTests headless (default sample:*)
                  rose analyze <mod.jar>                                   compatibility report for an old mod (exit 3 = problems)
                  rose foundry plan|run|status|show|pack|retry|reject ...  the AI foundry (see docs/FOUNDRY.md)
                  rose mcp                                                 MCP server on stdio (Claude Code etc.)

                Targets: client, server, gametest. Without --target, the first running one is used.
                Examples:
                  rose launch client
                  rose ctl client.createTestWorld
                  rose ctl world.setBlock x=0 y=-60 z=2 block=sample:counter_block
                  rose ctl client.useBlock x=0 y=-60 z=2
                  rose events --types overlay,chat
                """;
    }

    private RoseCli() {}
}

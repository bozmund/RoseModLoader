package rose.cli;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

/**
 * A Model Context Protocol server on stdin/stdout (newline-delimited JSON-RPC), so MCP clients such as Claude Code
 * can launch and drive Rose games. Configure it with the command {@code rose mcp}.
 *
 * <p>Tools are deliberately few and generic: {@code rose_methods} tells the model what the running game can do and
 * {@code rose_call} calls any of it. New bridge methods appear without changing this server.
 */
final class McpServer {
    private static final String PROTOCOL_VERSION = "2025-06-18";
    private static final Gson GSON = new Gson();

    private final Path home;
    private final PrintStream out;

    McpServer(Path home) {
        this.home = home;
        // stdout belongs to the protocol; anything else must go to stderr.
        this.out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        System.setOut(System.err);
    }

    void serve() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isBlank()) continue;
            JsonObject message;
            try {
                message = JsonParser.parseString(line).getAsJsonObject();
            } catch (RuntimeException e) {
                send(error(JsonNull.INSTANCE, -32700, "parse error"));
                continue;
            }
            if (!message.has("id")) continue; // notification (e.g. notifications/initialized)
            send(handle(message));
        }
    }

    private JsonObject handle(JsonObject request) {
        JsonElement id = request.get("id");
        String method = request.has("method") ? request.get("method").getAsString() : "";
        JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();
        try {
            JsonElement result = switch (method) {
                case "initialize" -> initialize(params);
                case "ping" -> new JsonObject();
                case "tools/list" -> tools();
                case "tools/call" -> callTool(params.get("name").getAsString(),
                        params.has("arguments") ? params.getAsJsonObject("arguments") : new JsonObject());
                default -> null;
            };
            if (result == null) return error(id, -32601, "method not found: " + method);
            JsonObject response = new JsonObject();
            response.addProperty("jsonrpc", "2.0");
            response.add("id", id);
            response.add("result", result);
            return response;
        } catch (Exception e) {
            return error(id, -32603, e.getMessage());
        }
    }

    private static JsonObject initialize(JsonObject params) {
        JsonObject result = new JsonObject();
        String requested = params.has("protocolVersion") ? params.get("protocolVersion").getAsString() : PROTOCOL_VERSION;
        result.addProperty("protocolVersion", requested);
        JsonObject capabilities = new JsonObject();
        capabilities.add("tools", new JsonObject());
        result.add("capabilities", capabilities);
        JsonObject info = new JsonObject();
        info.addProperty("name", "rose");
        info.addProperty("version", "0.0.1");
        result.add("serverInfo", info);
        result.addProperty("instructions", """
                Rose Mod Loader games (Minecraft 26.3). Typical flow: rose_launch {target:"client"} -> rose_methods ->
                rose_call {method:"client.createTestWorld"} -> rose_call world.setBlock / client.useBlock / bot.spawn ->
                rose_events to read chat, action-bar and log output -> rose_screenshot to look -> rose_stop.
                Run in-game tests with rose_test. All game interaction goes through rose_call.""");
        return result;
    }

    private static JsonObject tools() {
        JsonArray tools = new JsonArray();
        tools.add(tool("rose_launch", "Start a Rose game with the Agent Bridge on and wait until it answers (can take a few minutes the first time).",
                props("target", "string", "client (with window) or server (dedicated; needs eula=true in run/server/eula.txt)"),
                List.of("target")));
        tools.add(tool("rose_status", "Which Rose games are running right now (client, server, gametest).", new JsonObject(), List.of()));
        tools.add(tool("rose_methods", "List every method the running game offers, with parameters and descriptions.",
                props("target", "string", "client, server or gametest; optional"), List.of()));
        JsonObject callProps = props("method", "string", "bridge method name, e.g. world.getBlock (see rose_methods)");
        callProps.add("params", schema("object", "the method's parameters as a JSON object"));
        callProps.add("target", schema("string", "client, server or gametest; optional"));
        tools.add(tool("rose_call", "Call one Agent Bridge method in the running game and return its JSON result.", callProps, List.of("method")));
        JsonObject eventProps = props("after", "integer", "only events after this sequence number (use lastSeq from the previous call)");
        eventProps.add("types", schema("array", "filter: log, chat, overlay, player_join, server_started, server_stopping"));
        eventProps.add("target", schema("string", "optional"));
        tools.add(tool("rose_events", "Recent game events (log lines, chat, action-bar text, joins).", eventProps, List.of()));
        tools.add(tool("rose_screenshot", "Take a screenshot of the client window and return the image.", new JsonObject(), List.of()));
        tools.add(tool("rose_stop", "Stop a running game.", props("target", "string", "client or server"), List.of("target")));
        tools.add(tool("rose_test", "Run GameTests headless (no window, no EULA) and return pass/fail per test.",
                props("selector", "string", "namespaced wildcard, e.g. sample:* ; default runs sample:*"), List.of()));
        JsonObject result = new JsonObject();
        result.add("tools", tools);
        return result;
    }

    private JsonObject callTool(String name, JsonObject args) {
        try {
            String target = args.has("target") ? args.get("target").getAsString() : null;
            return switch (name) {
                case "rose_launch" -> text(Launcher.launch(home, target != null ? target : "client", 600, List.of())
                        .call("rose.ping", new JsonObject()));
                case "rose_status" -> text(status());
                case "rose_methods" -> text(Bridge.connect(home, target).call("rose.methods", new JsonObject()));
                case "rose_call" -> text(Bridge.connect(home, target).call(args.get("method").getAsString(),
                        args.has("params") && args.get("params").isJsonObject() ? args.getAsJsonObject("params") : new JsonObject()));
                case "rose_events" -> {
                    JsonObject params = new JsonObject();
                    if (args.has("after")) params.add("after", args.get("after"));
                    if (args.has("types")) params.add("types", args.get("types"));
                    yield text(Bridge.connect(home, target).call("events.poll", params));
                }
                case "rose_screenshot" -> screenshot();
                case "rose_stop" -> {
                    Bridge bridge = Bridge.connect(home, target);
                    RoseCli.stop(home, bridge);
                    yield text(GSON.toJsonTree("stopped " + bridge.target()));
                }
                case "rose_test" -> text(GameTests.runForJson(home, args.has("selector") ? args.get("selector").getAsString() : null));
                default -> toolError("unknown tool " + name);
            };
        } catch (Exception e) {
            return toolError(e.getMessage());
        }
    }

    private JsonElement status() {
        JsonObject out = new JsonObject();
        for (String target : Bridge.TARGETS) {
            boolean running;
            try {
                running = Files.exists(Bridge.discoveryFile(home, target)) && Bridge.connect(home, target).alive();
            } catch (Exception e) {
                running = false;
            }
            out.addProperty(target, running);
        }
        return out;
    }

    private JsonObject screenshot() throws Exception {
        JsonObject result = Bridge.connect(home, "client").call("client.screenshot", new JsonObject()).getAsJsonObject();
        byte[] png = Files.readAllBytes(Path.of(result.get("path").getAsString()));
        JsonObject image = new JsonObject();
        image.addProperty("type", "image");
        image.addProperty("data", Base64.getEncoder().encodeToString(png));
        image.addProperty("mimeType", "image/png");
        JsonArray content = new JsonArray();
        content.add(image);
        JsonObject out = new JsonObject();
        out.add("content", content);
        return out;
    }

    private static JsonObject text(JsonElement json) {
        JsonObject block = new JsonObject();
        block.addProperty("type", "text");
        block.addProperty("text", RoseCli.PRETTY.toJson(json));
        JsonArray content = new JsonArray();
        content.add(block);
        JsonObject out = new JsonObject();
        out.add("content", content);
        return out;
    }

    private static JsonObject toolError(String message) {
        JsonObject out = text(GSON.toJsonTree(message));
        out.addProperty("isError", true);
        return out;
    }

    private static JsonObject tool(String name, String description, JsonObject properties, List<String> required) {
        JsonObject inputSchema = new JsonObject();
        inputSchema.addProperty("type", "object");
        inputSchema.add("properties", properties);
        JsonArray req = new JsonArray();
        required.forEach(req::add);
        inputSchema.add("required", req);
        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);
        tool.add("inputSchema", inputSchema);
        return tool;
    }

    private static JsonObject props(String name, String type, String description) {
        JsonObject props = new JsonObject();
        props.add(name, schema(type, description));
        return props;
    }

    private static JsonObject schema(String type, String description) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", type);
        schema.addProperty("description", description);
        if (type.equals("array")) {
            JsonObject items = new JsonObject();
            items.addProperty("type", "string");
            schema.add("items", items);
        }
        return schema;
    }

    private void send(JsonObject message) {
        out.println(GSON.toJson(message));
    }

    private static JsonObject error(JsonElement id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("error", error);
        return response;
    }
}

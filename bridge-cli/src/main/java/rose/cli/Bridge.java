package rose.cli;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** JSON-RPC client for a running game's Agent Bridge, found through its {@code rose-bridge.json}. */
final class Bridge {
    static final String DISCOVERY_FILE = "rose-bridge.json";
    static final List<String> TARGETS = List.of("client", "server", "gametest");

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final AtomicLong IDS = new AtomicLong();

    private final String target;
    private final int port;
    private final String token;

    private Bridge(String target, int port, String token) {
        this.target = target;
        this.port = port;
        this.token = token;
    }

    String target() {
        return target;
    }

    static Path discoveryFile(Path home, String target) {
        return home.resolve("run").resolve(target).resolve(DISCOVERY_FILE);
    }

    /** Connects to {@code target}, or to the first running target (client, server, gametest) if it's null. */
    static Bridge connect(Path home, String target) throws IOException {
        List<String> candidates = target != null ? List.of(target) : TARGETS;
        List<String> tried = new ArrayList<>();
        for (String candidate : candidates) {
            Path file = discoveryFile(home, candidate);
            tried.add(file.toString());
            if (!Files.exists(file)) continue;
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            Bridge bridge = new Bridge(candidate, json.get("port").getAsInt(), json.get("token").getAsString());
            if (target != null || bridge.alive()) return bridge;
        }
        throw new IOException("No running game with the Agent Bridge found (looked for " + String.join(", ", tried)
                + "). Start one with: rose launch client|server|gametest");
    }

    boolean alive() {
        try {
            call("rose.ping", new JsonObject());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Calls a bridge method. Throws {@link BridgeException} with the game's error message on failure. */
    JsonElement call(String method, JsonObject params) throws IOException, InterruptedException {
        JsonObject request = new JsonObject();
        request.addProperty("jsonrpc", "2.0");
        request.addProperty("id", IDS.incrementAndGet());
        request.addProperty("method", method);
        request.add("params", params);
        HttpRequest http = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/rpc"))
                .timeout(Duration.ofMinutes(5))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(request)))
                .build();
        HttpResponse<String> response = HTTP.send(http, HttpResponse.BodyHandlers.ofString());
        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        if (body.has("error")) {
            JsonObject error = body.getAsJsonObject("error");
            throw new BridgeException(error.get("code").getAsInt(), error.get("message").getAsString());
        }
        return body.get("result");
    }

    static final class BridgeException extends IOException {
        final int code;

        BridgeException(int code, String message) {
            super(message);
            this.code = code;
        }
    }
}

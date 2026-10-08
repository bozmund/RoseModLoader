package rose.bridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * JSON-RPC 2.0 over HTTP on 127.0.0.1 only, guarded by a random per-launch token.
 *
 * <p>The port and token are written to {@code rose-bridge.json} in the game directory, which only local users with
 * access to that folder can read. {@code rose ctl} and {@code rose mcp} find the game through that file.
 */
public final class BridgeServer {
    public static final String DISCOVERY_FILE = "rose-bridge.json";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    private final Map<String, BridgeMethod> methods = new ConcurrentSkipListMap<>();
    private final String token = HexFormat.of().formatHex(new SecureRandom().generateSeed(24));
    private final String side;
    private final Path discoveryFile;
    private HttpServer http;

    public BridgeServer(String side, Path gameDir) {
        this.side = side;
        this.discoveryFile = gameDir.resolve(DISCOVERY_FILE);
    }

    public void register(String name, String params, String description, BridgeMethod.Handler handler) {
        if (methods.putIfAbsent(name, new BridgeMethod(name, params, description, handler)) != null) {
            throw new IllegalStateException("Bridge method already registered: " + name);
        }
    }

    public Map<String, BridgeMethod> methods() {
        return methods;
    }

    public String side() {
        return side;
    }

    public synchronized void start() throws IOException {
        http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        http.createContext("/rpc", this::handle);
        http.setExecutor(Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "Rose Agent Bridge");
            t.setDaemon(true);
            return t;
        }));
        // HttpServer's dispatcher thread inherits daemon status from the thread that starts it. Start it from a
        // daemon thread so the bridge never keeps the JVM alive after the game quits.
        Thread starter = new Thread(http::start, "Rose Agent Bridge start");
        starter.setDaemon(true);
        starter.start();
        try {
            starter.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while starting the bridge", e);
        }

        JsonObject discovery = new JsonObject();
        discovery.addProperty("port", http.getAddress().getPort());
        discovery.addProperty("token", token);
        discovery.addProperty("side", side);
        discovery.addProperty("pid", ProcessHandle.current().pid());
        Files.writeString(discoveryFile, GSON.toJson(discovery));
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Rose Agent Bridge shutdown"));
        BridgeMod.LOGGER.info("[rose_bridge] Agent Bridge listening on 127.0.0.1:{} ({})", http.getAddress().getPort(), discoveryFile);
    }

    public synchronized void stop() {
        if (http == null) return;
        http.stop(0);
        http = null;
        try {
            Files.deleteIfExists(discoveryFile);
        } catch (IOException ignored) {
            // best effort during shutdown
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"POST".equals(exchange.getRequestMethod())) {
                send(exchange, 405, error(JsonNull.INSTANCE, -32600, "use POST"));
                return;
            }
            if (!authorized(exchange.getRequestHeaders().getFirst("Authorization"))) {
                send(exchange, 401, error(JsonNull.INSTANCE, -32001, "missing or wrong bearer token (see " + DISCOVERY_FILE + ")"));
                return;
            }
            JsonObject request;
            try {
                request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                        .getAsJsonObject();
            } catch (RuntimeException e) {
                send(exchange, 200, error(JsonNull.INSTANCE, -32700, "request is not a JSON object"));
                return;
            }
            send(exchange, 200, dispatch(request));
        }
    }

    JsonObject dispatch(JsonObject request) {
        JsonElement id = request.has("id") ? request.get("id") : JsonNull.INSTANCE;
        String name = request.has("method") ? request.get("method").getAsString() : "";
        BridgeMethod method = methods.get(name);
        if (method == null) return error(id, -32601, "unknown method '" + name + "'; call rose.methods for the list");
        JsonObject params = request.has("params") && request.get("params").isJsonObject()
                ? request.getAsJsonObject("params") : new JsonObject();
        try {
            JsonElement result = method.handler().call(new Params(params));
            JsonObject response = new JsonObject();
            response.addProperty("jsonrpc", "2.0");
            response.add("id", id);
            response.add("result", result != null ? result : JsonNull.INSTANCE);
            return response;
        } catch (Params.InvalidParams e) {
            return error(id, -32602, e.getMessage());
        } catch (Exception e) {
            Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
            BridgeMod.LOGGER.warn("[rose_bridge] {} failed", name, cause);
            return error(id, -32000, cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
    }

    private boolean authorized(String header) {
        String expected = "Bearer " + token;
        return header != null && MessageDigest.isEqual(header.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
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

    private static void send(HttpExchange exchange, int status, JsonObject body) throws IOException {
        byte[] bytes = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}

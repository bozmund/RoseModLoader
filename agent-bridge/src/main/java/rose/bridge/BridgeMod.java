package rose.bridge;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rose.api.ModInitializer;
import rose.api.event.RoseEvents;
import rose.bridge.methods.BotMethods;
import rose.bridge.methods.CommonMethods;
import rose.bridge.methods.ServerMethods;
import rose.loader.RoseLoader;

/**
 * Starts the Agent Bridge when the JVM runs with {@code -Drose.bridge=true} (Rose's dev tasks set it). Off otherwise:
 * in normal play nothing listens.
 */
public final class BridgeMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("rose_bridge");

    private static BridgeServer bridge;
    private static volatile MinecraftServer server;

    public static boolean enabled() {
        return Boolean.getBoolean("rose.bridge");
    }

    /** The running bridge, or {@code null} when disabled. */
    public static BridgeServer bridge() {
        return bridge;
    }

    /** The running server (dedicated, GameTest or the client's integrated server). */
    public static MinecraftServer requireServer() {
        MinecraftServer current = server;
        if (current == null) {
            throw new IllegalStateException("no server is running (on the client: open a world first, e.g. client.createTestWorld)");
        }
        return current;
    }

    public static MinecraftServer serverOrNull() {
        return server;
    }

    @Override
    public void onInitialize() {
        if (!enabled()) return;
        String side = RoseLoader.get().side().name().toLowerCase(java.util.Locale.ROOT);
        bridge = new BridgeServer(side, Path.of("").toAbsolutePath());

        RoseEvents.SERVER_STARTED.register(s -> {
            server = s;
            EventLog.add("server_started", new JsonObject());
        });
        RoseEvents.SERVER_STOPPING.register(s -> {
            EventLog.add("server_stopping", new JsonObject());
            BotMethods.forgetAll();
            server = null;
        });
        RoseEvents.PLAYER_JOIN.register(p -> EventLog.add("player_join", "name", p.getName().getString()));

        CommonMethods.register(bridge);
        ServerMethods.register(bridge);
        BotMethods.register(bridge);

        LogCapture.install();
        // The client starts the bridge once its client methods exist (BridgeClientMod), so callers that wait for
        // the bridge never see a half-registered method list.
        if (RoseLoader.get().side() != RoseLoader.Side.CLIENT) startBridge();
    }

    public static void startBridge() {
        try {
            bridge.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the Rose Agent Bridge", e);
        }
    }
}

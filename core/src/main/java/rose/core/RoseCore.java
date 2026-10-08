package rose.core;

import java.util.concurrent.atomic.AtomicBoolean;
import rose.api.ModInitializer;
import rose.api.client.ClientModInitializer;
import rose.loader.RoseLoader;

/** Runs mod entrypoints at the right moments. Called from Rose's Mixin hooks. */
public final class RoseCore {
    private static final AtomicBoolean MAIN_DONE = new AtomicBoolean();
    private static final AtomicBoolean CLIENT_DONE = new AtomicBoolean();

    /** Just before vanilla freezes its built-in registries. */
    public static void runMainEntrypoints() {
        if (!MAIN_DONE.compareAndSet(false, true)) return;
        for (var entry : RoseLoader.get().entrypoints("main", ModInitializer.class)) {
            run(entry.mod().id(), "main", entry.instance()::onInitialize);
        }
    }

    /** At the end of the client's {@code Minecraft} constructor. */
    public static void runClientEntrypoints() {
        if (!CLIENT_DONE.compareAndSet(false, true)) return;
        for (var entry : RoseLoader.get().entrypoints("client", ClientModInitializer.class)) {
            run(entry.mod().id(), "client", entry.instance()::onInitializeClient);
        }
    }

    private static void run(String modId, String kind, Runnable entrypoint) {
        try {
            entrypoint.run();
        } catch (RuntimeException | Error e) {
            throw new IllegalStateException("Mod '" + modId + "' failed in its " + kind + " entrypoint", e);
        }
    }

    private RoseCore() {}
}

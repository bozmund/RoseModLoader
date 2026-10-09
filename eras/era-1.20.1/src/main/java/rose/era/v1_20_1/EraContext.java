package rose.era.v1_20_1;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import rose.api.ModInitializer;
import rose.api.event.RoseEvents;

/**
 * What old code took as parameters that 26.3 no longer passes around (a registry access for item stacks and
 * recipes). Tracks the running server; before one exists, built-in registries are used.
 */
public final class EraContext implements ModInitializer {
    private static volatile MinecraftServer server;

    @Override
    public void onInitialize() {
        RoseEvents.SERVER_STARTED.register(s -> server = s);
        RoseEvents.SERVER_STOPPING.register(s -> server = null);
    }

    /** Called from the server's first tick setup too (before SERVER_STARTED), see ServerMixin. */
    public static void setServer(MinecraftServer s) {
        server = s;
    }

    public static MinecraftServer server() {
        return server;
    }

    public static RegistryAccess registryAccess() {
        MinecraftServer s = server;
        return s != null ? s.registryAccess() : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}

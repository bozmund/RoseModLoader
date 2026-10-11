package rose.dialect.forge.v1_20_1;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import rose.api.event.RoseEvents;

/** Fires Forge server lifecycle and tick events from Rose core hooks. */
public final class ForgeServerEvents {
    static void register() {
        RoseEvents.SERVER_STARTED.register(server -> {
            MinecraftForge.EVENT_BUS.post(new ServerStartingEvent(server));
            MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(server));
        });
        RoseEvents.SERVER_TICK_END.register(server -> {
            MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END, server));
            net.minecraftforge.common.WorldWorkerManager.tick(false);
        });
        RoseEvents.SERVER_STOPPING.register(server -> {
            MinecraftForge.EVENT_BUS.post(new ServerStoppingEvent(server));
            net.minecraftforge.common.WorldWorkerManager.clear();
            RegistryAccessHolder.setServer(null);
        });
    }

    public static void stopped(MinecraftServer server) {
        if (ForgeDialect.mods().isEmpty()) return;
        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.server.ServerStoppedEvent(server));
    }

    public static void aboutToStart(MinecraftServer server) {
        RegistryAccessHolder.setServer(server);
        if (ForgeDialect.mods().isEmpty()) return;
        net.minecraftforge.server.permission.PermissionAPI.initializePermissionAPI();
        MinecraftForge.EVENT_BUS.post(new ServerAboutToStartEvent(server));
        ForgeTrades.load(); // Forge's VillagerTradingManager listens to ServerAboutToStartEvent
        BiomeModifiers.apply(server);
    }

    private ForgeServerEvents() {}
}

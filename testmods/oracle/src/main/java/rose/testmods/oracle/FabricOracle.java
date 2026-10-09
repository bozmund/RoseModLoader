package rose.testmods.oracle;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * Fabric entrypoint for the reference server (Fabric 26.3 + the mods' native ports). Only the oracle-fabric jar
 * contains it. The server stops after the dump: it exists only to produce the answer key.
 */
public final class FabricOracle implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!OracleDump.requested()) return;
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            OracleDump.runFromProperties(server);
            server.halt(false);
        });
    }
}

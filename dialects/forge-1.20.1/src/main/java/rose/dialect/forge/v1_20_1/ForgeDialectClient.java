package rose.dialect.forge.v1_20_1;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import rose.api.client.ClientModInitializer;
import rose.api.network.client.RoseClientNetworking;

/** Client side of FML loading: client setup, then load complete, once the game window exists. */
public final class ForgeDialectClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (ForgeDialect.mods().isEmpty()) return;
        RoseClientNetworking.receiveOnClient(MenuData.TYPE, (payload, client) -> MenuData.received(payload.data()));
        ForgeDialect.dispatchLifecycle(FMLClientSetupEvent::new);
        ForgeDialect.dispatchLifecycle(FMLLoadCompleteEvent::new);
    }
}

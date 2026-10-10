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
        // Forge asked items for these as they were constructed, so before any client event
        rose.dialect.forge.v1_20_1.client.ClientItemExtensions.collect();
        // Forge fired this before renderers are built (the first resource reload builds them from the registrations).
        ForgeDialect.postToMods(new net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers(), "RegisterRenderers");
        ForgeDialect.dispatchLifecycle(FMLClientSetupEvent::new);
        ForgeDialect.dispatchLifecycle(FMLLoadCompleteEvent::new);
    }
}

package rose.testmods.sample.client;

import net.minecraft.network.chat.Component;
import rose.api.client.ClientModInitializer;
import rose.api.network.client.RoseClientNetworking;
import rose.testmods.sample.CountPayload;
import rose.testmods.sample.SampleMod;

public final class SampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RoseClientNetworking.receiveOnClient(CountPayload.TYPE, (payload, client) -> {
            if (client.player != null) {
                client.player.sendOverlayMessage(Component.literal("Counter: " + payload.count()));
            }
            SampleMod.LOGGER.info("[sample] client received count {} at {}", payload.count(), payload.pos());
        });
        SampleMod.LOGGER.info("[sample] client initialized");
    }
}

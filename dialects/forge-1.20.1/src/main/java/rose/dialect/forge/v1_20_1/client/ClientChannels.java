package rose.dialect.forge.v1_20_1.client;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import rose.api.network.client.RoseClientNetworking;

/** Client-only networking calls, kept out of common classes so the dedicated server never loads them. */
public final class ClientChannels {
    public static <T extends CustomPacketPayload> void receive(CustomPacketPayload.Type<T> type, Consumer<T> handler) {
        RoseClientNetworking.receiveOnClient(type, (payload, context) -> handler.accept(payload));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        RoseClientNetworking.sendToServer(payload);
    }

    private ClientChannels() {}
}

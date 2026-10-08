package rose.api.network.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import rose.api.network.RoseNetworking;

/** Client half of {@link RoseNetworking}. Only use from client code. */
public final class RoseClientNetworking {
    private static final Map<Identifier, ClientReceiver<?>> RECEIVERS = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface ClientReceiver<T extends CustomPacketPayload> {
        void receive(T payload, Minecraft client);
    }

    public static <T extends CustomPacketPayload> void receiveOnClient(CustomPacketPayload.Type<T> type, ClientReceiver<T> receiver) {
        RoseNetworking.Registration<?> registration = RoseNetworking.registration(type.id());
        if (registration == null || registration.direction() != RoseNetworking.Direction.SERVER_TO_CLIENT) {
            throw new IllegalStateException("Register " + type.id() + " with registerServerToClient before adding a receiver");
        }
        if (RECEIVERS.putIfAbsent(type.id(), receiver) != null) {
            throw new IllegalStateException("A client receiver for " + type.id() + " already exists");
        }
    }

    public static void sendToServer(CustomPacketPayload payload) {
        RoseNetworking.Registration<?> registration = RoseNetworking.registration(payload.type().id());
        if (registration == null || registration.direction() != RoseNetworking.Direction.CLIENT_TO_SERVER) {
            throw new IllegalStateException("Payload " + payload.type().id() + " is not registered client-to-server");
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) throw new IllegalStateException("Not connected to a server");
        connection.send(new ServerboundCustomPayloadPacket(payload));
    }

    /** The client receiver for a payload id, or {@code null}. Used by Rose core. */
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> ClientReceiver<T> receiver(Identifier id) {
        return (ClientReceiver<T>) RECEIVERS.get(id);
    }

    private RoseClientNetworking() {}
}

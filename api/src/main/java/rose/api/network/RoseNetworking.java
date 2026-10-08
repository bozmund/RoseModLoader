package rose.api.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Custom packets ("payloads") during gameplay.
 *
 * <ol>
 *   <li>Register the payload type and codec on <b>both</b> sides, in a {@link rose.api.ModInitializer}:
 *       {@link #registerServerToClient} or {@link #registerClientToServer}.</li>
 *   <li>Register a receiver on the receiving side: {@link #receiveOnServer} here, or
 *       {@link rose.api.network.client.RoseClientNetworking#receiveOnClient} on the client.</li>
 *   <li>Send with {@link #sendToPlayer} or {@code RoseClientNetworking.sendToServer}.</li>
 * </ol>
 *
 * Receivers run on the game thread (server thread or client render thread), never on the network thread.
 */
public final class RoseNetworking {
    private static final Map<Identifier, Registration<?>> TYPES = new ConcurrentHashMap<>();
    private static final Map<Identifier, ServerReceiver<?>> SERVER_RECEIVERS = new ConcurrentHashMap<>();

    public enum Direction { SERVER_TO_CLIENT, CLIENT_TO_SERVER }

    public record Registration<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Direction direction) {}

    @FunctionalInterface
    public interface ServerReceiver<T extends CustomPacketPayload> {
        void receive(T payload, ServerPlayer player);
    }

    public static <T extends CustomPacketPayload> void registerServerToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        register(new Registration<>(type, codec, Direction.SERVER_TO_CLIENT));
    }

    public static <T extends CustomPacketPayload> void registerClientToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        register(new Registration<>(type, codec, Direction.CLIENT_TO_SERVER));
    }

    private static void register(Registration<?> registration) {
        Registration<?> previous = TYPES.putIfAbsent(registration.type().id(), registration);
        if (previous != null) {
            throw new IllegalStateException("Payload type already registered: " + registration.type().id());
        }
    }

    public static <T extends CustomPacketPayload> void receiveOnServer(CustomPacketPayload.Type<T> type, ServerReceiver<T> receiver) {
        Registration<?> registration = TYPES.get(type.id());
        if (registration == null || registration.direction() != Direction.CLIENT_TO_SERVER) {
            throw new IllegalStateException("Register " + type.id() + " with registerClientToServer before adding a receiver");
        }
        if (SERVER_RECEIVERS.putIfAbsent(type.id(), receiver) != null) {
            throw new IllegalStateException("A server receiver for " + type.id() + " already exists");
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        requireRegistered(payload, Direction.SERVER_TO_CLIENT);
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    /** The registration for a payload id, or {@code null}. Used by Rose core's codec hook. */
    public static Registration<?> registration(Identifier id) {
        return TYPES.get(id);
    }

    /** The server receiver for a payload id, or {@code null}. Used by Rose core. */
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> ServerReceiver<T> serverReceiver(Identifier id) {
        return (ServerReceiver<T>) SERVER_RECEIVERS.get(id);
    }

    static void requireRegistered(CustomPacketPayload payload, Direction direction) {
        Registration<?> registration = TYPES.get(payload.type().id());
        if (registration == null || registration.direction() != direction) {
            throw new IllegalStateException("Payload " + payload.type().id() + " is not registered for " + direction);
        }
    }

    private RoseNetworking() {}
}

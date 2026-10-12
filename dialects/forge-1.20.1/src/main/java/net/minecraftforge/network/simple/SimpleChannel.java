package net.minecraftforge.network.simple;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import rose.api.network.RoseNetworking;
import rose.dialect.forge.v1_20_1.client.ClientChannels;

/**
 * Forge's indexed message channel, carried by two Rose payload types ({@code <channel>/to_server} and
 * {@code <channel>/to_client}). Messages are encoded inside the payload codec, so they get the connection's
 * registry-aware buffer just like vanilla packets.
 */
public class SimpleChannel {
    private final Identifier name;
    private final Map<Integer, IndexedMessageCodec.MessageHandler<?>> byIndex = new ConcurrentHashMap<>();
    private final Map<Class<?>, IndexedMessageCodec.MessageHandler<?>> byClass = new ConcurrentHashMap<>();
    private final CustomPacketPayload.Type<Message> toServer;
    private final CustomPacketPayload.Type<Message> toClient;

    /** One message on the wire: the handler index and the message object. */
    public record Message(CustomPacketPayload.Type<Message> type, int index, Object message) implements CustomPacketPayload {}

    public SimpleChannel(Identifier name) {
        this.name = name;
        this.toServer = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(name.getNamespace(), name.getPath() + "/to_server"));
        this.toClient = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(name.getNamespace(), name.getPath() + "/to_client"));
        RoseNetworking.registerClientToServer(toServer, codec(toServer));
        RoseNetworking.registerServerToClient(toClient, codec(toClient));
        RoseNetworking.receiveOnServer(toServer, (payload, player) -> handle(payload, NetworkDirection.PLAY_TO_SERVER, player));
        if (FMLEnvironment.dist.isClient()) {
            ClientChannels.receive(toClient, payload -> handle(payload, NetworkDirection.PLAY_TO_CLIENT, null));
        }
    }

    public Identifier getName() {
        return name;
    }

    private StreamCodec<RegistryFriendlyByteBuf, Message> codec(CustomPacketPayload.Type<Message> type) {
        return StreamCodec.of((buf, payload) -> {
            buf.writeVarInt(payload.index());
            encode(handler(payload.index()), payload.message(), buf);
        }, buf -> {
            int index = buf.readVarInt();
            // 1.20.1 handed decoders the payload in a buffer of its own, which lived until the message was handled
            // (SpectreLib keeps it) and whose unread rest didn't matter; 26.3 frees the packet buffer after decoding.
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            RegistryFriendlyByteBuf own = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(bytes), buf.registryAccess());
            return new Message(type, index, handler(index).decoder.apply(own));
        });
    }

    @SuppressWarnings("unchecked")
    private static <MSG> void encode(IndexedMessageCodec.MessageHandler<MSG> handler, Object message, FriendlyByteBuf buf) {
        handler.encoder.accept((MSG) message, buf);
    }

    private IndexedMessageCodec.MessageHandler<?> handler(int index) {
        IndexedMessageCodec.MessageHandler<?> handler = byIndex.get(index);
        if (handler == null) throw new IllegalArgumentException("Channel " + name + " has no message with index " + index);
        return handler;
    }

    @SuppressWarnings("unchecked")
    private <MSG> void handle(Message payload, NetworkDirection direction, ServerPlayer sender) {
        IndexedMessageCodec.MessageHandler<MSG> handler = (IndexedMessageCodec.MessageHandler<MSG>) handler(payload.index());
        NetworkEvent.Context context = new NetworkEvent.Context(direction, sender);
        handler.consumer.accept((MSG) payload.message(), () -> context);
    }

    public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType,
            BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        IndexedMessageCodec.MessageHandler<MSG> handler = new IndexedMessageCodec.MessageHandler<>(index, messageType, encoder, decoder, messageConsumer);
        if (byIndex.putIfAbsent(index, handler) != null) throw new IllegalArgumentException("Duplicate message index " + index + " on " + name);
        byClass.put(messageType, handler);
        return handler;
    }

    public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType,
            BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, Optional<NetworkDirection> direction) {
        return registerMessage(index, messageType, encoder, decoder, messageConsumer);
    }

    private int indexOf(Object message) {
        IndexedMessageCodec.MessageHandler<?> handler = byClass.get(message.getClass());
        if (handler == null) throw new IllegalArgumentException("Channel " + name + " has no message type " + message.getClass().getName());
        return handler.index;
    }

    public <MSG> void sendToServer(MSG message) {
        ClientChannels.sendToServer(new Message(toServer, indexOf(message), message));
    }

    public <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        boolean toTheServer = target.getDirection() == NetworkDirection.PLAY_TO_SERVER;
        target.send(new Message(toTheServer ? toServer : toClient, indexOf(message), message));
    }

    public <MSG> void sendTo(MSG message, ServerPlayer player) {
        RoseNetworking.sendToPlayer(player, new Message(toClient, indexOf(message), message));
    }

    /** To whoever is at the other end of {@code connection}: the server, or the player that connection belongs to. */
    public <MSG> void sendTo(MSG message, Connection connection, NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_SERVER) {
            sendToServer(message);
            return;
        }
        var server = rose.dialect.forge.v1_20_1.RegistryAccessHolder.server();
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection != null && player.connection.connection == connection) sendTo(message, player);
        }
    }
}

package rose.dialect.forge.v1_20_1;

import io.netty.buffer.Unpooled;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import rose.api.network.RoseNetworking;

/**
 * Extra data for menus opened with NetworkHooks.openScreen. The server sends it as a Rose payload right before
 * vanilla's open-screen packet (same connection, so it arrives first); the client keeps it until the menu
 * factory asks for it.
 */
public final class MenuData {
    public static final CustomPacketPayload.Type<Payload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("rose_forge_1_20_1", "menu_data"));
    private static final AtomicReference<byte[]> PENDING = new AtomicReference<>();

    public record Payload(byte[] data) implements CustomPacketPayload {
        static final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC =
                ByteBufCodecs.BYTE_ARRAY.<RegistryFriendlyByteBuf>cast().map(Payload::new, Payload::data);

        @Override
        public Type<Payload> type() {
            return TYPE;
        }
    }

    static void register() {
        RoseNetworking.registerServerToClient(TYPE, Payload.CODEC);
    }

    public static void sendBeforeOpen(ServerPlayer player, byte[] data) {
        RoseNetworking.sendToPlayer(player, new Payload(data));
    }

    /** Client: remember the data for the next menu. */
    public static void received(byte[] data) {
        PENDING.set(data);
    }

    /** Client: the extra data for the menu being created (empty if the server sent none). */
    public static FriendlyByteBuf take(int windowId) {
        byte[] data = PENDING.getAndSet(null);
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data != null ? data : new byte[0]));
    }

    private MenuData() {}
}

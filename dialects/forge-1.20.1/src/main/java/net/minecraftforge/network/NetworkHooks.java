package net.minecraftforge.network;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import rose.dialect.forge.v1_20_1.MenuData;
import rose.dialect.forge.v1_20_1.Unsupported;

public final class NetworkHooks {
    public static void openScreen(ServerPlayer player, MenuProvider provider) {
        openScreen(player, provider, buf -> {});
    }

    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        openScreen(player, provider, buf -> buf.writeBlockPos(pos));
    }

    /** Opens a menu and gives the client menu factory the extra data (sent just before the open-screen packet). */
    public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraDataWriter) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        extraDataWriter.accept(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        MenuData.sendBeforeOpen(player, data);
        player.openMenu(provider);
    }

    /** 26.3 entities build their own spawn packet from their ServerEntity; old overrides are no longer called. */
    public static Packet<?> getEntitySpawningPacket(Entity entity) {
        Unsupported.feature("entity-spawn-packet", "NetworkHooks.getEntitySpawningPacket is not needed on 26.3 (vanilla spawn packets are used)");
        return null;
    }

    private NetworkHooks() {}
}

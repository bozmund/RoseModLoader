package rose.core.mixin;

import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.api.network.RoseNetworking;

/** Vanilla ignores custom payloads from clients during gameplay; Rose hands registered ones to their receiver. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void rose$handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        CustomPacketPayload payload = packet.payload();
        RoseNetworking.ServerReceiver<CustomPacketPayload> receiver = RoseNetworking.serverReceiver(payload.type().id());
        if (receiver == null) return;
        // Re-queues this packet on the server thread (by throwing) when called from the network thread.
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        receiver.receive(payload, player);
    }
}

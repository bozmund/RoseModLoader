package rose.core.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.api.network.client.RoseClientNetworking;

/** Vanilla logs unknown payloads; Rose hands registered ones to their receiver (already on the client thread). */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
            at = @At("HEAD"), cancellable = true)
    private void rose$handleCustomPayload(CustomPacketPayload payload, CallbackInfo ci) {
        RoseClientNetworking.ClientReceiver<CustomPacketPayload> receiver = RoseClientNetworking.receiver(payload.type().id());
        if (receiver == null) return;
        receiver.receive(payload, Minecraft.getInstance());
        ci.cancel();
    }
}

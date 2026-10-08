package rose.core.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rose.core.network.RosePayloadCodecs;

/** Wraps the gameplay codec's fallback (the first codec built in the static initializer). */
@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketMixin {
    @ModifyArg(method = "<clinit>", index = 0, at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static CustomPacketPayload.FallbackProvider<FriendlyByteBuf> rose$wrapGameplayFallback(
            CustomPacketPayload.FallbackProvider<FriendlyByteBuf> vanilla) {
        return RosePayloadCodecs.wrap(vanilla);
    }
}

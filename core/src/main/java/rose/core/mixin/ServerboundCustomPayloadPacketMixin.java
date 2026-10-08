package rose.core.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rose.core.network.RosePayloadCodecs;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketMixin {
    @ModifyArg(method = "<clinit>", index = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static CustomPacketPayload.FallbackProvider<FriendlyByteBuf> rose$wrapFallback(
            CustomPacketPayload.FallbackProvider<FriendlyByteBuf> vanilla) {
        return RosePayloadCodecs.wrap(vanilla);
    }
}

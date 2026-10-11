package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 ClientPlayerNetworkEvent.LoggingIn once the login packet set up the player. */
@Mixin(ClientPacketListener.class)
public abstract class ClientConnectionEventsMixin {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void rose$loggingIn(ClientboundLoginPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ForgeDialect.active() || minecraft.player == null || minecraft.gameMode == null) return;
        MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(minecraft.gameMode, minecraft.player,
                ((ClientPacketListener) (Object) this).getConnection()));
    }
}

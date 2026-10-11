package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 ClientTickEvent around each client tick, and ClientPlayerNetworkEvent.LoggingOut when leaving a world. */
@Mixin(Minecraft.class)
public abstract class ClientEventsMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void rose$tickStart(CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void rose$tickEnd(CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
    }

    @Inject(method = "clearClientLevel", at = @At("HEAD"))
    private void rose$loggingOut(net.minecraft.client.gui.screens.Screen screen, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        if (!ForgeDialect.active() || minecraft.player == null) return;
        var listener = minecraft.getConnection();
        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut(minecraft.gameMode, minecraft.player,
                listener == null ? null : listener.getConnection()));
    }
}

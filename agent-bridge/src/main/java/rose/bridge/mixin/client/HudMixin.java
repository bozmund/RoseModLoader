package rose.bridge.mixin.client;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.bridge.BridgeMod;
import rose.bridge.EventLog;

/** The action-bar text above the hotbar ("overlay message") becomes an {@code overlay} event. */
@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void rose$overlay(Component message, boolean animate, CallbackInfo ci) {
        if (BridgeMod.bridge() != null) EventLog.add("overlay", "text", message.getString());
    }
}

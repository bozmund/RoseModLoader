package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/** Forge 1.20.1 InputEvent.Key after vanilla handled a key (ForgeHooksClient.onKeyInput). */
@Mixin(KeyboardHandler.class)
public abstract class KeyInputEventsMixin {
    @Inject(method = "keyPress", at = @At("TAIL"))
    private void rose$keyInput(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (!ForgeDialect.active() || !((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(InputEvent.Key.class)) return;
        MinecraftForge.EVENT_BUS.post(new InputEvent.Key(event.key(), event.keycode(), action, event.modifiers()));
    }
}

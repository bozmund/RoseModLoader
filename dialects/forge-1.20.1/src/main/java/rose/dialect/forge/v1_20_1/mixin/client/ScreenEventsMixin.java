package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.RoseEventBus;

/** Forge 1.20.1 ScreenEvent.Init and ScreenEvent.Render around a screen's setup and drawing. */
@Mixin(Screen.class)
public abstract class ScreenEventsMixin {
    @Inject(method = "init(II)V", at = @At("TAIL"))
    private void rose$initPost(int width, int height, CallbackInfo ci) {
        post(new ScreenEvent.Init.Post((Screen) (Object) this));
    }

    @Inject(method = "rebuildWidgets", at = @At("TAIL"))
    private void rose$rebuilt(CallbackInfo ci) {
        post(new ScreenEvent.Init.Post((Screen) (Object) this));
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"), cancellable = true)
    private void rose$renderPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (listened(ScreenEvent.Render.Pre.class)
                && MinecraftForge.EVENT_BUS.post(new ScreenEvent.Render.Pre((Screen) (Object) this, graphics, mouseX, mouseY, a))) ci.cancel();
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void rose$renderPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (listened(ScreenEvent.Render.Post.class)) MinecraftForge.EVENT_BUS.post(new ScreenEvent.Render.Post((Screen) (Object) this, graphics, mouseX, mouseY, a));
    }

    private static void post(ScreenEvent event) {
        if (listened(event.getClass())) MinecraftForge.EVENT_BUS.post(event);
    }

    private static boolean listened(Class<? extends net.minecraftforge.eventbus.api.Event> type) {
        return ForgeDialect.active() && ((RoseEventBus) MinecraftForge.EVENT_BUS).hasListeners(type);
    }
}

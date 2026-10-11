package rose.era.v1_20_1.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.client.LegacyWidgetRender;

/**
 * A 1.20.1 widget's own render replaces the final extractRenderState, as its override replaced render. While it
 * runs, its {@code super.render} call (now extractRenderState) draws as vanilla does.
 */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetRenderMixin {
    @Unique
    private boolean rose$inLegacyRender;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rose$legacyRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!((Object) this instanceof LegacyWidgetRender legacy) || rose$inLegacyRender) return;
        rose$inLegacyRender = true;
        try {
            legacy.legacy$render(graphics, mouseX, mouseY, a);
        } finally {
            rose$inLegacyRender = false;
        }
        ci.cancel();
    }
}

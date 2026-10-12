package rose.era.v1_20_1.mixin.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.client.LegacyWidgetRender;

/**
 * 1.20.1 widget drawing on 26.3's final extractRenderState. A widget overriding 1.20.1 {@code render} (renamed to
 * legacy$render, see LegacyWidgetRender) draws through it instead; while it runs, its {@code super.render} draws as
 * vanilla does. A widget overriding 1.20.1 {@code renderWidget} (which 26.3 never calls) gets 1.20.1's render: hover
 * state, its renderWidget, the tooltip. {@code super.renderWidget} lands on {@link #renderWidget}, the 26.3 drawing.
 */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetRenderMixin {
    @Unique
    private static final Map<Class<?>, Boolean> rose$overridesRenderWidget = new ConcurrentHashMap<>();

    @Unique
    private boolean rose$inLegacyRender;

    @Shadow
    public boolean visible;

    @Shadow
    protected boolean isHovered;

    @Shadow
    private boolean areCoordinatesInRectangle(double x, double y) {
        return false;
    }

    @Shadow
    protected abstract void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);

    @Shadow
    protected abstract void extractTooltipForNextRenderPass(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

    /** 1.20.1 {@code AbstractWidget.renderWidget}, for old widgets' {@code super.renderWidget}: the widget's 26.3 drawing. */
    public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rose$legacyRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (rose$inLegacyRender) return;
        if ((Object) this instanceof LegacyWidgetRender legacy) {
            rose$inLegacyRender = true;
            try {
                legacy.legacy$render(graphics, mouseX, mouseY, a);
            } finally {
                rose$inLegacyRender = false;
            }
            ci.cancel();
        } else if (rose$overridesRenderWidget.computeIfAbsent(getClass(), AbstractWidgetRenderMixin::rose$declaresRenderWidget)) {
            if (visible) {
                isHovered = graphics.containsPointInScissor(mouseX, mouseY) && areCoordinatesInRectangle(mouseX, mouseY);
                renderWidget(graphics, mouseX, mouseY, a);
                extractTooltipForNextRenderPass(graphics, mouseX, mouseY);
            }
            ci.cancel();
        }
    }

    /** Whether mod code (not the game or Rose) overrides {@code renderWidget} somewhere in {@code type}'s hierarchy. */
    @Unique
    private static boolean rose$declaresRenderWidget(Class<?> type) {
        for (Class<?> c = type; c != null && c != AbstractWidget.class; c = c.getSuperclass()) {
            if (c.getName().startsWith("net.minecraft.") || c.getName().startsWith("rose.")) continue;
            try {
                c.getDeclaredMethod("renderWidget", GuiGraphicsExtractor.class, int.class, int.class, float.class);
                return true;
            } catch (NoSuchMethodException e) {
                // keep looking up
            }
        }
        return false;
    }
}

package rose.era.v1_20_1.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A mod widget that overrides 1.20.1 {@code AbstractWidget.render}, final in 26.3 (as extractRenderState): the
 * translator renames its override to {@link #legacy$render}, which AbstractWidgetRenderMixin calls instead.
 */
public interface LegacyWidgetRender {
    void legacy$render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);
}

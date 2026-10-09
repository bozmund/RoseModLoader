package rose.era.v1_20_1.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** A 1.20.1 image button: its texture region (and the region yDiffTex below when hovered) instead of sprites. */
public class LegacyImageButton extends ImageButton {
    private final Identifier texture;
    private final int u;
    private final int v;
    private final int yDiffTex;

    public LegacyImageButton(int x, int y, int width, int height, int u, int v, int yDiffTex, Identifier texture, Button.OnPress onPress) {
        super(x, y, width, height, new WidgetSprites(texture), onPress);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.yDiffTex = yDiffTex;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int vOffset = v + (isHoveredOrFocused() ? yDiffTex : 0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), u, vOffset, width, height, 256, 256);
    }
}

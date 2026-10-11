package rose.era.v1_20_1.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 1.20.1's ImageButton (mods' {@code ImageButton} is renamed to this): cut from a texture at (xTexStart, yTexStart),
 * yDiffTex further down when hovered and twice that when inactive, instead of 26.3's sprites. Mods subclass it and
 * override {@link #renderWidget}, which is what draws it.
 */
public class LegacyImageButton extends ImageButton {
    protected final Identifier resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, Identifier texture, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, height, texture, 256, 256, onPress);
    }

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier texture, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, texture, 256, 256, onPress);
    }

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier texture,
            int textureWidth, int textureHeight, Button.OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, texture, textureWidth, textureHeight, onPress, CommonComponents.EMPTY);
    }

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, Identifier texture,
            int textureWidth, int textureHeight, Button.OnPress onPress, Component message) {
        super(x, y, width, height, new WidgetSprites(texture), onPress, message);
        this.resourceLocation = texture;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    /** A button drawn from 26.3 sprites (vanilla textures that became sprites, such as the recipe book toggle). */
    public LegacyImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress) {
        super(x, y, width, height, sprites, onPress, CommonComponents.EMPTY);
        this.resourceLocation = null;
        this.xTexStart = 0;
        this.yTexStart = 0;
        this.yDiffTex = 0;
        this.textureWidth = 256;
        this.textureHeight = 256;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        renderWidget(graphics, mouseX, mouseY, a);
    }

    /** 1.20.1 {@code renderWidget}: draws the button. */
    public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (resourceLocation == null) {
            super.extractContents(graphics, mouseX, mouseY, a);
            return;
        }
        renderTexture(graphics, resourceLocation, getX(), getY(), xTexStart, yTexStart, yDiffTex, width, height, textureWidth, textureHeight);
    }

    /** 1.20.1 {@code AbstractWidget.renderTexture}. */
    public void renderTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int yDiffTex,
            int width, int height, int textureWidth, int textureHeight) {
        int offsetV = v;
        if (!isActive()) {
            offsetV += yDiffTex * 2;
        } else if (isHoveredOrFocused()) {
            offsetV += yDiffTex;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, offsetV, width, height, textureWidth, textureHeight);
    }
}

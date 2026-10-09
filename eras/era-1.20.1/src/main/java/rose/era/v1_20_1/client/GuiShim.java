package rose.era.v1_20_1.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Redirect targets for 1.20.1 GUI drawing. 26.x renamed GuiGraphics to GuiGraphicsExtractor (drawing records render
 * state) and changed several signatures.
 */
public final class GuiShim {
    /** 1.20.1 {@code blit(texture, x, y, u, v, width, height)} sampled a 256x256 texture. */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 256, 256);
    }

    /**
     * 1.20.1 {@code drawString(font, text, x, y, color, shadow)} returned the end x. Colors without alpha were
     * opaque then; 1.21.6 text needs ARGB (alpha 0 is invisible).
     */
    public static int drawString(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color, boolean shadow) {
        g.text(font, text, x, y, opaque(color), shadow);
        return x + font.width(text) + (shadow ? 1 : 0);
    }

    /** 1.20.1 screens drew their dimmed background themselves; since 1.20.2 Screen does it before rendering. */
    public static void renderBackground(Screen self, GuiGraphicsExtractor g) {
    }

    /** 1.20.1 {@code RenderSystem.setShaderColor}: a global tint for later draws; 26.3 passes colors per draw. */
    public static void setShaderColor(float r, float g, float b, float a) {
    }

    /** 1.20.1 {@code new ImageButton(x, y, w, h, u, v, yDiffTex, texture, onPress)}: a button cut from a texture. */
    public static ImageButton imageButton(int x, int y, int width, int height, int u, int v, int yDiffTex, Identifier texture, Button.OnPress onPress) {
        LegacyImageButton button = new LegacyImageButton(x, y, width, height, u, v, yDiffTex, texture, onPress);
        // The vanilla recipe book button opens a mod's recipe book, which is a hidden LegacyRecipeBookComponent on
        // 26.3; a button that does nothing would only confuse players.
        if (texture.equals(RECIPE_BUTTON)) button.visible = false;
        return button;
    }

    /** 1.20.1 vanilla texture of the recipe book toggle (26.3 draws it from the recipe_book/button sprites). */
    private static final Identifier RECIPE_BUTTON = Identifier.withDefaultNamespace("textures/gui/recipe_button.png");

    static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    private GuiShim() {}
}

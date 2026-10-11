package rose.era.v1_20_1.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
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

    /** 1.20.1 {@code blit(texture, x, y, u, v, width, height, textureWidth, textureHeight)}. */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, float u, float v, int width, int height,
                            int textureWidth, int textureHeight) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /** 1.20.1 {@code blit(texture, x, y, blitOffset, u, v, width, height, textureWidth, textureHeight)}: 26.3 GUI has no z offset. */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int blitOffset, float u, float v, int width, int height,
                            int textureWidth, int textureHeight) {
        blit(g, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /** 1.20.1 {@code blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight)}: a scaled region. */
    public static void blit(GuiGraphicsExtractor g, Identifier texture, int x, int y, int width, int height, float u, float v,
                            int uWidth, int vHeight, int textureWidth, int textureHeight) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight);
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

    /** 1.20.1 {@code RenderSystem.setShader(supplier)}: 26.3 GUI draws pick their pipeline per call (GUI_TEXTURED for blit). */
    public static void setShader(java.util.function.Supplier<?> shader) {
    }

    /** 1.20.1 {@code RenderSystem.setShaderTexture(unit, texture)}: 26.3 blit takes the texture itself. */
    public static void setShaderTexture(int unit, Identifier texture) {
    }

    /** 1.20.1 {@code GameRenderer.getPositionTexShader()}, passed to setShader (which ignores it). */
    public static com.mojang.renderpearl.backend.opengl.GlProgram positionTexShader() {
        return null;
    }

    /** 1.20.1 {@code new ImageButton(x, y, w, h, u, v, yDiffTex, texture, onPress)}: a button cut from a texture. */
    public static LegacyImageButton imageButton(int x, int y, int width, int height, int u, int v, int yDiffTex, Identifier texture, Button.OnPress onPress) {
        // vanilla's recipe book toggle: 26.3 draws it from sprites
        if (texture.equals(RECIPE_BUTTON)) return new LegacyImageButton(x, y, width, height, RecipeBookComponent.RECIPE_BUTTON_SPRITES, onPress);
        return new LegacyImageButton(x, y, width, height, u, v, yDiffTex, texture, onPress);
    }

    /** 1.20.1 vanilla texture of the recipe book toggle (26.3 draws it from the recipe_book/button sprites). */
    private static final Identifier RECIPE_BUTTON = Identifier.withDefaultNamespace("textures/gui/recipe_button.png");

    static int opaque(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    private GuiShim() {}

    /** 1.20.1 {@code Gui.getGuiTicks()}: 26.x moved the HUD (and its tick count) to {@code Gui.hud}. */
    public static int getGuiTicks(net.minecraft.client.gui.Gui self) {
        return self.hud.getGuiTicks();
    }

    /** 1.20.1 {@code options.hideGui} (F1); 26.x keeps it on the HUD. */
    public static boolean hideGui(net.minecraft.client.Options self) {
        return net.minecraft.client.Minecraft.getInstance().gui.hud.isHidden();
    }

    public static void hideGui(net.minecraft.client.Options self, boolean hidden) {
        var hud = net.minecraft.client.Minecraft.getInstance().gui.hud;
        if (hud.isHidden() != hidden) hud.toggle();
    }

    /** 1.20.1 {@code options.renderDebug} (F3); 26.x asks the debug overlay. */
    public static boolean renderDebug(net.minecraft.client.Options self) {
        return net.minecraft.client.Minecraft.getInstance().getDebugOverlay().showDebugScreen();
    }

    /** 1.20.1 {@code minecraft.screen}; 26.x keeps the screen on {@code Gui}. */
    public static net.minecraft.client.gui.screens.Screen screen(net.minecraft.client.Minecraft self) {
        return self.gui.screen();
    }

    // 1.20.1 GUI code set global GL blend and depth state around drawing; 26.x GUI pipelines carry their own.

    public static void enableBlend() {
    }

    public static void disableBlend() {
    }

    public static void defaultBlendFunc() {
    }

    public static void blendFunc(int source, int destination) {
    }

    public static void enableDepthTest() {
    }

    public static void disableDepthTest() {
    }
}

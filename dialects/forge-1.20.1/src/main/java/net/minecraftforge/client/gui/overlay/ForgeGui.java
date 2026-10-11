package net.minecraftforge.client.gui.overlay;

import net.minecraft.client.Minecraft;

/**
 * Forge's HUD. Forge made {@code Minecraft.gui} a ForgeGui; on Rose, 26.3's Gui implements this interface
 * (GuiForgeMixin), so mods' {@code (ForgeGui) minecraft.gui} casts hold. The layout heights mods read and add to
 * ({@code leftHeight}, {@code rightHeight}) live in {@link rose.dialect.forge.v1_20_1.client.HudOverlays}.
 */
public interface ForgeGui {
    default Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    default boolean shouldDrawSurvivalElements() {
        var player = Minecraft.getInstance().player;
        return player != null && Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.canHurtPlayer();
    }

    default net.minecraft.client.gui.Font getFont() {
        return Minecraft.getInstance().font;
    }

    default void setupOverlayRenderState(boolean blend, boolean depthTest) {
        // 26.3 GUI drawing has no global blend/depth state
    }
}

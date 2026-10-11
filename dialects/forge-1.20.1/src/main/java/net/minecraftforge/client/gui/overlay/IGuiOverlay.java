package net.minecraftforge.client.gui.overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A mod's HUD element (Forge 1.20.1 IGuiOverlay; 1.20.1 GuiGraphics is 26.3's GuiGraphicsExtractor). */
@FunctionalInterface
public interface IGuiOverlay {
    void render(ForgeGui gui, GuiGraphicsExtractor guiGraphics, float partialTick, int screenWidth, int screenHeight);
}

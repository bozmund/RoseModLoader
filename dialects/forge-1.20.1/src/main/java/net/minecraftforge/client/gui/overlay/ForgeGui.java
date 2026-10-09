package net.minecraftforge.client.gui.overlay;

import net.minecraft.client.Minecraft;

/** Forge's HUD with layout state for mods drawing next to vanilla bars. */
public class ForgeGui {
    public int leftHeight = 39;
    public int rightHeight = 39;

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public boolean shouldDrawSurvivalElements() {
        var player = Minecraft.getInstance().player;
        return player != null && !player.isCreative() && !player.isSpectator();
    }
}

package net.minecraftforge.client.gui.overlay;

import net.minecraft.resources.Identifier;

public final class GuiOverlayManager {
    public static NamedGuiOverlay findOverlay(Identifier id) {
        return new NamedGuiOverlay(id);
    }

    private GuiOverlayManager() {}
}

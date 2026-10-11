package net.minecraftforge.client.gui.overlay;

import java.util.List;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import rose.dialect.forge.v1_20_1.client.HudOverlays;

public final class GuiOverlayManager {
    /** The overlay registered under {@code id} (vanilla's or a mod's), or null. */
    public static @Nullable NamedGuiOverlay findOverlay(Identifier id) {
        return HudOverlays.find(id);
    }

    public static List<NamedGuiOverlay> getOverlays() {
        return HudOverlays.all();
    }

    private GuiOverlayManager() {}
}

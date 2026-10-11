package net.minecraftforge.client.gui.overlay;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A HUD element by id (e.g. {@code minecraft:player_health}); one instance per id (GuiOverlayManager), as Forge's
 * were, so mods compare them with {@code ==}. Vanilla elements have no overlay of their own here.
 */
public record NamedGuiOverlay(Identifier id, @Nullable IGuiOverlay overlay) {
}

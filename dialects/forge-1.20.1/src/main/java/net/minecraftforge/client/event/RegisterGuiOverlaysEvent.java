package net.minecraftforge.client.event;

import net.minecraft.resources.Identifier;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.dialect.forge.v1_20_1.client.HudOverlays;

/** Mod bus: register HUD overlays, drawn relative to vanilla's elements (ids get the mod's namespace). */
public class RegisterGuiOverlaysEvent extends Event implements IModBusEvent {
    public void registerBelowAll(String id, IGuiOverlay overlay) {
        HudOverlays.register(id, overlay, null, false);
    }

    public void registerBelow(Identifier other, String id, IGuiOverlay overlay) {
        HudOverlays.register(id, overlay, other, false);
    }

    public void registerAbove(Identifier other, String id, IGuiOverlay overlay) {
        HudOverlays.register(id, overlay, other, true);
    }

    public void registerAboveAll(String id, IGuiOverlay overlay) {
        HudOverlays.register(id, overlay, null, true);
    }

    public void registerBelow(VanillaGuiOverlay other, String id, IGuiOverlay overlay) {
        registerBelow(other.id(), id, overlay);
    }

    public void registerAbove(VanillaGuiOverlay other, String id, IGuiOverlay overlay) {
        registerAbove(other.id(), id, overlay);
    }
}

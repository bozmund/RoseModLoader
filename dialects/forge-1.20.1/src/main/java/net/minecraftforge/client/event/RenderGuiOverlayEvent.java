package net.minecraftforge.client.event;

import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Around drawing one HUD element. 26.3 draws the HUD through GuiGraphicsExtractor (render state extraction), so
 * the 1.20.1 GuiGraphics accessor comes with the era bridge's GuiGraphics shim; not fired yet.
 */
public abstract class RenderGuiOverlayEvent extends Event {
    private final NamedGuiOverlay overlay;

    protected RenderGuiOverlayEvent(NamedGuiOverlay overlay) {
        this.overlay = overlay;
    }

    public NamedGuiOverlay getOverlay() {
        return overlay;
    }

    @Cancelable
    public static class Pre extends RenderGuiOverlayEvent {
        public Pre(NamedGuiOverlay overlay) {
            super(overlay);
        }
    }

    public static class Post extends RenderGuiOverlayEvent {
        public Post(NamedGuiOverlay overlay) {
            super(overlay);
        }
    }
}

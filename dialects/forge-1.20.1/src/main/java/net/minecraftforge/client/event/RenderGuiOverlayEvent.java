package net.minecraftforge.client.event;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Around drawing one HUD element (1.20.1 GuiGraphics is 26.3's GuiGraphicsExtractor). Canceling Pre skips it. */
public abstract class RenderGuiOverlayEvent extends Event {
    private final Window window;
    private final GuiGraphicsExtractor guiGraphics;
    private final float partialTick;
    private final NamedGuiOverlay overlay;

    protected RenderGuiOverlayEvent(Window window, GuiGraphicsExtractor guiGraphics, float partialTick, NamedGuiOverlay overlay) {
        this.window = window;
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
        this.overlay = overlay;
    }

    public Window getWindow() {
        return window;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public NamedGuiOverlay getOverlay() {
        return overlay;
    }

    @Cancelable
    public static class Pre extends RenderGuiOverlayEvent {
        public Pre(Window window, GuiGraphicsExtractor guiGraphics, float partialTick, NamedGuiOverlay overlay) {
            super(window, guiGraphics, partialTick, overlay);
        }
    }

    public static class Post extends RenderGuiOverlayEvent {
        public Post(Window window, GuiGraphicsExtractor guiGraphics, float partialTick, NamedGuiOverlay overlay) {
            super(window, guiGraphics, partialTick, overlay);
        }
    }
}

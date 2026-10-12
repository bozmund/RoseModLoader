package net.minecraftforge.client.event;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraftforge.eventbus.api.Event;

/** Changes to a vanilla HUD element before it is drawn (1.20.1 GuiGraphics is 26.3's GuiGraphicsExtractor). */
public abstract class CustomizeGuiOverlayEvent extends Event {
    private final Window window;
    private final GuiGraphicsExtractor guiGraphics;
    private final float partialTick;

    protected CustomizeGuiOverlayEvent(Window window, GuiGraphicsExtractor guiGraphics, float partialTick) {
        this.window = window;
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
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

    /** The F3 screen's text: lines added to {@link #getLeft()} / {@link #getRight()} are shown in its columns. */
    public static class DebugText extends CustomizeGuiOverlayEvent {
        private final ArrayList<String> left;
        private final ArrayList<String> right;

        public DebugText(Window window, GuiGraphicsExtractor guiGraphics, float partialTick, ArrayList<String> left, ArrayList<String> right) {
            super(window, guiGraphics, partialTick);
            this.left = left;
            this.right = right;
        }

        public ArrayList<String> getLeft() {
            return left;
        }

        public ArrayList<String> getRight() {
            return right;
        }
    }
}

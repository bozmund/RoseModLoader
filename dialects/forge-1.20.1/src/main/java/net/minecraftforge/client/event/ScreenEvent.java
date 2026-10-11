package net.minecraftforge.client.event;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import rose.dialect.forge.v1_20_1.mixin.client.ScreenAccessor;

/** A screen's lifecycle, drawing and input (Forge bus); 1.20.1 GuiGraphics is 26.3's GuiGraphicsExtractor. */
public abstract class ScreenEvent extends Event {
    private final Screen screen;

    protected ScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    /** Around a screen setting up its widgets (first shown, or rebuilt). */
    public abstract static class Init extends ScreenEvent {
        protected Init(Screen screen) {
            super(screen);
        }

        public List<GuiEventListener> getListenersList() {
            return List.copyOf(getScreen().children());
        }

        /** Adds a widget: drawn and narrated too when it is one of those. */
        public void addListener(GuiEventListener listener) {
            ScreenAccessor screen = (ScreenAccessor) getScreen();
            if (listener instanceof net.minecraft.client.gui.components.Renderable && listener instanceof net.minecraft.client.gui.narration.NarratableEntry) {
                screen.rose$addRenderableWidget(listener);
            } else if (listener instanceof net.minecraft.client.gui.narration.NarratableEntry) {
                screen.rose$addWidget(listener);
            } else {
                ((ScreenAccessor) getScreen()).rose$children().add(listener);
            }
        }

        public void removeListener(GuiEventListener listener) {
            ((ScreenAccessor) getScreen()).rose$removeWidget(listener);
        }

        @Cancelable
        public static class Pre extends Init {
            public Pre(Screen screen) {
                super(screen);
            }
        }

        public static class Post extends Init {
            public Post(Screen screen) {
                super(screen);
            }
        }
    }

    /** Around drawing a screen. Canceling Pre skips it. */
    public abstract static class Render extends ScreenEvent {
        private final GuiGraphicsExtractor guiGraphics;
        private final int mouseX;
        private final int mouseY;
        private final float partialTick;

        protected Render(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            super(screen);
            this.guiGraphics = guiGraphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.partialTick = partialTick;
        }

        public GuiGraphicsExtractor getGuiGraphics() {
            return guiGraphics;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }

        public float getPartialTick() {
            return partialTick;
        }

        @Cancelable
        public static class Pre extends Render {
            public Pre(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super(screen, guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        public static class Post extends Render {
            public Post(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                super(screen, guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /** A mouse button pressed over a screen. Canceling Pre keeps it from the screen. */
    public abstract static class MouseButtonPressed extends ScreenEvent {
        private final double mouseX;
        private final double mouseY;
        private final int button;

        protected MouseButtonPressed(Screen screen, double mouseX, double mouseY, int button) {
            super(screen);
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.button = button;
        }

        public double getMouseX() {
            return mouseX;
        }

        public double getMouseY() {
            return mouseY;
        }

        public int getButton() {
            return button;
        }

        @Cancelable
        public static class Pre extends MouseButtonPressed {
            public Pre(Screen screen, double mouseX, double mouseY, int button) {
                super(screen, mouseX, mouseY, button);
            }
        }

        public static class Post extends MouseButtonPressed {
            private final boolean handled;

            public Post(Screen screen, double mouseX, double mouseY, int button, boolean handled) {
                super(screen, mouseX, mouseY, button);
                this.handled = handled;
            }

            public boolean wasHandled() {
                return handled;
            }
        }
    }
}

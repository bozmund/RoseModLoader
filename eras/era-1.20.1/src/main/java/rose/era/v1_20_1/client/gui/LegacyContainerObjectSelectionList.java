package rose.era.v1_20_1.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import rose.era.v1_20_1.client.LegacyKeyCodes;

/**
 * 1.20.1's {@code ContainerObjectSelectionList} (see LegacySelectionList): rows holding widgets (26.3 widgets, such as
 * buttons and edit boxes), which get the input the rows get. Rows are never shown as selected.
 */
public abstract class LegacyContainerObjectSelectionList<E extends LegacyContainerObjectSelectionList.Entry<E>> extends LegacySelectionList<E> {
    public LegacyContainerObjectSelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (getItemCount() == 0) return null;
        if (!(event instanceof FocusNavigationEvent.ArrowNavigation arrow)) return super.nextFocusPath(event);
        E focused = getFocused();
        if (arrow.direction().getAxis() == ScreenAxis.HORIZONTAL && focused != null) {
            return ComponentPath.path(this, focused.nextFocusPath(event));
        }
        int column = focused == null ? -1 : focused.children().indexOf(focused.getFocused());
        ScreenDirection direction = arrow.direction();
        if (column == -1) {
            switch (direction) {
                case LEFT -> {
                    column = Integer.MAX_VALUE;
                    direction = ScreenDirection.DOWN;
                }
                case RIGHT -> {
                    column = 0;
                    direction = ScreenDirection.DOWN;
                }
                default -> column = 0;
            }
        }
        E entry = focused;
        ComponentPath path;
        do {
            entry = nextEntry(direction, e -> !e.children().isEmpty(), entry);
            if (entry == null) return null;
            path = entry.focusPathAtIndex(arrow, column);
        } while (path == null);
        return ComponentPath.path(this, path);
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        if (focused == null) setSelected(null);
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return isFocused() ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        E entry = getHovered() != null ? getHovered() : getFocused();
        if (entry != null) {
            entry.updateNarration(output.nest());
            narrateListElementPosition(output, entry);
        }
        output.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
    }

    /**
     * 1.20.1's {@code ContainerObjectSelectionList.Entry}: its {@code children()} get the row's input, as 26.3 events,
     * like a 26.3 container.
     */
    public abstract static class Entry<E extends Entry<E>> extends LegacySelectionList.Entry<E> implements ContainerEventHandler {
        private @Nullable GuiEventListener focused;
        private @Nullable NarratableEntry lastNarratable;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return dragging;
        }

        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if (this.focused != null) this.focused.setFocused(false);
            if (focused != null) focused.setFocused(true);
            this.focused = focused;
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return super.isFocused();
        }

        @Override
        public @Nullable GuiEventListener getFocused() {
            return focused;
        }

        @Override
        public boolean isMouseOver(double x, double y) {
            return super.isMouseOver(x, y);
        }

        public @Nullable ComponentPath focusPathAtIndex(FocusNavigationEvent event, int index) {
            if (children().isEmpty()) return null;
            return ComponentPath.path(this, children().get(Math.min(index, children().size() - 1)).nextFocusPath(event));
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
            if (event instanceof FocusNavigationEvent.ArrowNavigation arrow) {
                int step = switch (arrow.direction()) {
                    case LEFT -> -1;
                    case RIGHT -> 1;
                    case UP, DOWN -> 0;
                };
                if (step == 0) return null;
                int start = Mth.clamp(step + children().indexOf(getFocused()), 0, children().size() - 1);
                for (int i = start; i >= 0 && i < children().size(); i += step) {
                    ComponentPath path = children().get(i).nextFocusPath(event);
                    if (path != null) return ComponentPath.path(this, path);
                }
            }
            return ContainerEventHandler.super.nextFocusPath(event);
        }

        public abstract List<? extends NarratableEntry> narratables();

        void updateNarration(NarrationElementOutput output) {
            List<? extends NarratableEntry> narratables = narratables();
            Screen.NarratableSearchResult found = Screen.findNarratableWidget(narratables, lastNarratable);
            if (found == null) return;
            if (found.priority().isTerminal()) lastNarratable = found.entry();
            if (narratables.size() > 1) {
                output.add(NarratedElementType.POSITION, Component.translatable("narrator.position.object_list", found.index() + 1, narratables.size()));
                if (found.priority() == NarratableEntry.NarrationPriority.FOCUSED) {
                    output.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
                }
            }
            found.entry().updateNarration(output.nest());
        }

        // 1.20.1-shaped input from the list: the children get it as 26.3 events (26.3's container handling)

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return ContainerEventHandler.super.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(LegacyKeyCodes.mouseButton(button), 0)), false);
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            return ContainerEventHandler.super.mouseReleased(new MouseButtonEvent(x, y, new MouseButtonInfo(LegacyKeyCodes.mouseButton(button), 0)));
        }

        @Override
        public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
            return ContainerEventHandler.super.mouseDragged(new MouseButtonEvent(x, y, new MouseButtonInfo(LegacyKeyCodes.mouseButton(button), 0)), dx, dy);
        }

        @Override
        public boolean mouseScrolled(double x, double y, double delta) {
            return ContainerEventHandler.super.mouseScrolled(x, y, 0, delta);
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            return ContainerEventHandler.super.keyPressed(new KeyEvent(LegacyKeyCodes.key(key), scancode, modifiers));
        }

        @Override
        public boolean keyReleased(int key, int scancode, int modifiers) {
            return ContainerEventHandler.super.keyReleased(new KeyEvent(LegacyKeyCodes.key(key), scancode, modifiers));
        }

        @Override
        public boolean charTyped(char c, int modifiers) {
            return ContainerEventHandler.super.charTyped(new CharacterEvent(c));
        }

        // 26.3 events reaching the row go through the 1.20.1 shapes, which mods override
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return mouseClicked(event.x(), event.y(), LegacyKeyCodes.glfwMouseButton(event.button()));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            return mouseReleased(event.x(), event.y(), LegacyKeyCodes.glfwMouseButton(event.button()));
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
            return mouseDragged(event.x(), event.y(), LegacyKeyCodes.glfwMouseButton(event.button()), dx, dy);
        }

        @Override
        public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
            return mouseScrolled(x, y, scrollY);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            return keyPressed(LegacyKeyCodes.glfw(event.key()), event.keycode(), event.modifiers());
        }

        @Override
        public boolean keyReleased(KeyEvent event) {
            return keyReleased(LegacyKeyCodes.glfw(event.key()), event.keycode(), event.modifiers());
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            return Character.isBmpCodePoint(event.codepoint()) && charTyped((char) event.codepoint(), 0);
        }
    }
}

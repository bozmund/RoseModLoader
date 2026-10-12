package rose.era.v1_20_1.client.gui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import rose.era.v1_20_1.client.LegacyKeyCodes;

/**
 * 1.20.1's {@code AbstractSelectionList} (mods' references are renamed to this): a scrolling list of rows at fixed
 * edges (x0, y0, x1, y1), each {@code itemHeight} tall. 1.20.2 turned lists into widgets and later releases changed
 * their geometry, scrolling and entry drawing, all of which 1.20.1 mods subclass and call; this keeps the 1.20.1
 * contract (with Forge's edge getters) on 26.3 types. Member names are the ones Rosetta gives the old members.
 *
 * <p>Input: 26.3 hands the list input events, 1.20.1 code overrides and calls {@code mouseClicked(x, y, button)},
 * {@code keyPressed(key, scancode, modifiers)} (GLFW key codes) and so on; the event methods call those.
 */
public abstract class LegacySelectionList<E extends LegacySelectionList.Entry<E>> extends AbstractContainerEventHandler
        implements Renderable, NarratableEntry {
    private static final Identifier INWORLD_MENU_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_background.png");

    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new TrackedList();
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected boolean centerListVertically = true;
    private double scrollAmount;
    private boolean renderSelection = true;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    private @Nullable E selected;
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;
    private @Nullable E hovered;

    public LegacySelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.y0 = y0;
        this.y1 = y1;
        this.itemHeight = itemHeight;
        this.x0 = 0;
        this.x1 = width;
    }

    public void setRenderSelection(boolean renderSelection) {
        this.renderSelection = renderSelection;
    }

    protected void setRenderHeader(boolean renderHeader, int headerHeight) {
        this.renderHeader = renderHeader;
        this.headerHeight = renderHeader ? headerHeight : 0;
    }

    public int getRowWidth() {
        return 220;
    }

    public @Nullable E getSelected() {
        return selected;
    }

    public void setSelected(@Nullable E selected) {
        this.selected = selected;
    }

    public E getFirstElement() {
        return children.get(0);
    }

    public void setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    public void setRenderTopAndBottom(boolean renderTopAndBottom) {
        this.renderTopAndBottom = renderTopAndBottom;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable E getFocused() {
        return (E) super.getFocused();
    }

    @Override
    public final List<E> children() {
        return children;
    }

    protected void clearEntries() {
        children.clear();
        selected = null;
    }

    protected void replaceEntries(Collection<E> entries) {
        clearEntries();
        children.addAll(entries);
    }

    protected E getEntry(int index) {
        return children().get(index);
    }

    protected int addEntry(E entry) {
        children.add(entry);
        return children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
        double fromBottom = getMaxScroll() - getScrollAmount();
        children.add(0, entry);
        setScrollAmount(getMaxScroll() - fromBottom);
    }

    protected boolean removeEntryFromTop(E entry) {
        double fromBottom = getMaxScroll() - getScrollAmount();
        boolean removed = removeEntry(entry);
        setScrollAmount(getMaxScroll() - fromBottom);
        return removed;
    }

    protected int getItemCount() {
        return children().size();
    }

    protected boolean isSelectedItem(int index) {
        return Objects.equals(getSelected(), children().get(index));
    }

    protected final @Nullable E getEntryAtPosition(double x, double y) {
        int half = getRowWidth() / 2;
        int center = x0 + width / 2;
        int offset = Mth.floor(y - y0) - headerHeight + (int) getScrollAmount() - 4;
        int index = offset / itemHeight;
        boolean inRow = x < getScrollbarPosition() && x >= center - half && x <= center + half;
        return inRow && index >= 0 && offset >= 0 && index < getItemCount() ? children().get(index) : null;
    }

    public void updateSize(int width, int height, int y0, int y1) {
        this.width = width;
        this.height = height;
        this.y0 = y0;
        this.y1 = y1;
        this.x0 = 0;
        this.x1 = width;
    }

    public void setLeftPos(int x0) {
        this.x0 = x0;
        this.x1 = x0 + width;
    }

    // Forge 1.20.1's getters for the list's edges
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTop() {
        return y0;
    }

    public int getBottom() {
        return y1;
    }

    public int getLeft() {
        return x0;
    }

    public int getRight() {
        return x1;
    }

    protected int getMaxPosition() {
        return getItemCount() * itemHeight + headerHeight;
    }

    protected void clickedHeader(int x, int y) {
    }

    protected void renderHeader(GuiGraphicsExtractor graphics, int x, int y) {
    }

    protected void renderBackground(GuiGraphicsExtractor graphics) {
    }

    protected void renderDecorations(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    /** 1.20.1 {@code render}: background, rows, scroll bar. 1.20.1's dirt background is 26.3's menu background. */
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int scrollbarLeft = getScrollbarPosition();
        int scrollbarRight = scrollbarLeft + 6;
        hovered = isMouseOver(mouseX, mouseY) ? getEntryAtPosition(mouseX, mouseY) : null;
        if (renderBackground) {
            Screen.extractMenuBackgroundTexture(graphics, minecraft.level == null ? Screen.MENU_BACKGROUND : INWORLD_MENU_BACKGROUND,
                    x0, y0, x1, y1 + (float) (int) getScrollAmount(), x1 - x0, y1 - y0);
        }
        enableScissor(graphics);
        if (renderHeader) renderHeader(graphics, getRowLeft(), y0 + 4 - (int) getScrollAmount());
        renderListItems(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        if (renderTopAndBottom) {
            graphics.fillGradient(x0, y0, x1, y0 + 4, 0xFF000000, 0);
            graphics.fillGradient(x0, y1 - 4, x1, y1, 0, 0xFF000000);
        }
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int span = y1 - y0;
            int thumb = Mth.clamp((int) ((float) (span * span) / getMaxPosition()), 32, span - 8);
            int thumbTop = Math.max(y0, (int) getScrollAmount() * (span - thumb) / maxScroll + y0);
            graphics.fill(scrollbarLeft, y0, scrollbarRight, y1, 0xFF000000);
            graphics.fill(scrollbarLeft, thumbTop, scrollbarRight, thumbTop + thumb, 0xFF808080);
            graphics.fill(scrollbarLeft, thumbTop, scrollbarRight - 1, thumbTop + thumb - 1, 0xFFC0C0C0);
        }
        renderDecorations(graphics, mouseX, mouseY);
    }

    protected void enableScissor(GuiGraphicsExtractor graphics) {
        graphics.enableScissor(x0, y0, x1, y1);
    }

    protected void centerScrollOn(E entry) {
        setScrollAmount(children().indexOf(entry) * itemHeight + itemHeight / 2 - (y1 - y0) / 2);
    }

    protected void ensureVisible(E entry) {
        int top = getRowTop(children().indexOf(entry));
        int above = top - y0 - 4 - itemHeight;
        if (above < 0) scroll(above);
        int below = y1 - top - itemHeight - itemHeight;
        if (below < 0) scroll(-below);
    }

    private void scroll(int amount) {
        setScrollAmount(getScrollAmount() + amount);
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(double amount) {
        scrollAmount = Mth.clamp(amount, 0.0, getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - (y1 - y0 - 4));
    }

    public int getScrollBottom() {
        return (int) getScrollAmount() - height - headerHeight;
    }

    protected void updateScrollingState(double x, double y, int button) {
        scrolling = button == 0 && x >= getScrollbarPosition() && x < getScrollbarPosition() + 6;
    }

    protected int getScrollbarPosition() {
        return width / 2 + 124;
    }

    // 1.20.1-shaped input, which 1.20.1 lists and entries override and call

    public boolean mouseClicked(double x, double y, int button) {
        updateScrollingState(x, y, button);
        if (!isMouseOver(x, y)) return false;
        E entry = getEntryAtPosition(x, y);
        if (entry != null) {
            if (entry.mouseClicked(x, y, button)) {
                E focused = getFocused();
                if (focused != entry && focused instanceof ContainerEventHandler container) container.setFocused(null);
                setFocused(entry);
                setDragging(true);
                return true;
            }
        } else if (button == 0) {
            clickedHeader((int) (x - (x0 + width / 2 - getRowWidth() / 2)), (int) (y - y0) + (int) getScrollAmount() - 4);
            return true;
        }
        return scrolling;
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (getFocused() != null) getFocused().mouseReleased(x, y, button);
        return false;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(x, y, button, dx, dy)) return true;
        if (button != 0 || !scrolling) return false;
        if (y < y0) {
            setScrollAmount(0.0);
        } else if (y > y1) {
            setScrollAmount(getMaxScroll());
        } else {
            double maxScroll = Math.max(1, getMaxScroll());
            int span = y1 - y0;
            int thumb = Mth.clamp((int) ((float) (span * span) / getMaxPosition()), 32, span - 8);
            setScrollAmount(getScrollAmount() + dy * Math.max(1.0, maxScroll / (span - thumb)));
        }
        return true;
    }

    public boolean mouseScrolled(double x, double y, double delta) {
        setScrollAmount(getScrollAmount() - delta * itemHeight / 2.0);
        return true;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        return getFocused() != null && getFocused().keyPressed(key, scancode, modifiers);
    }

    public boolean keyReleased(int key, int scancode, int modifiers) {
        return getFocused() != null && getFocused().keyReleased(key, scancode, modifiers);
    }

    public boolean charTyped(char c, int modifiers) {
        return getFocused() != null && getFocused().charTyped(c, modifiers);
    }

    // 26.3 input events, to the 1.20.1 shapes above

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

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        int index = children.indexOf(focused);
        if (index >= 0) {
            E entry = children.get(index);
            setSelected(entry);
            if (minecraft.getLastInputType().isKeyboard()) ensureVisible(entry);
        }
    }

    protected @Nullable E nextEntry(ScreenDirection direction) {
        return nextEntry(direction, e -> true);
    }

    protected @Nullable E nextEntry(ScreenDirection direction, Predicate<E> accept) {
        return nextEntry(direction, accept, getSelected());
    }

    protected @Nullable E nextEntry(ScreenDirection direction, Predicate<E> accept, @Nullable E from) {
        int step = switch (direction) {
            case RIGHT, LEFT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };
        if (children().isEmpty() || step == 0) return null;
        int start = from == null ? (step > 0 ? 0 : children().size() - 1) : children().indexOf(from) + step;
        for (int i = start; i >= 0 && i < children.size(); i += step) {
            E entry = children().get(i);
            if (accept.test(entry)) return entry;
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return y >= y0 && y <= y1 && x >= x0 && x <= x1;
    }

    /** 1.20.1 {@code renderList}. */
    protected void renderListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = getRowLeft();
        int rowWidth = getRowWidth();
        int rowHeight = itemHeight - 4;
        for (int i = 0; i < getItemCount(); i++) {
            int top = getRowTop(i);
            if (getRowBottom(i) >= y0 && top <= y1) renderItem(graphics, mouseX, mouseY, partialTick, i, left, top, rowWidth, rowHeight);
        }
    }

    protected void renderItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top,
                              int rowWidth, int rowHeight) {
        E entry = getEntry(index);
        boolean isHovered = Objects.equals(hovered, entry);
        entry.renderBack(graphics, index, top, left, rowWidth, rowHeight, mouseX, mouseY, isHovered, partialTick);
        if (renderSelection && isSelectedItem(index)) {
            renderSelection(graphics, top, rowWidth, rowHeight, isFocused() ? 0xFFFFFFFF : 0xFF808080, 0xFF000000);
        }
        entry.renderContent(graphics, index, top, left, rowWidth, rowHeight, mouseX, mouseY, isHovered, partialTick);
    }

    protected void renderSelection(GuiGraphicsExtractor graphics, int top, int rowWidth, int rowHeight, int border, int fill) {
        int left = x0 + (width - rowWidth) / 2;
        int right = x0 + (width + rowWidth) / 2;
        graphics.fill(left, top - 2, right, top + rowHeight + 2, border);
        graphics.fill(left + 1, top - 1, right - 1, top + rowHeight + 1, fill);
    }

    public int getRowLeft() {
        return x0 + width / 2 - getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return getRowLeft() + getRowWidth();
    }

    protected int getRowTop(int index) {
        return y0 + 4 - (int) getScrollAmount() + index * itemHeight + headerHeight;
    }

    protected int getRowBottom(int index) {
        return getRowTop(index) + itemHeight;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (isFocused()) return NarratableEntry.NarrationPriority.FOCUSED;
        return hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    protected @Nullable E remove(int index) {
        E entry = children.get(index);
        return removeEntry(entry) ? entry : null;
    }

    protected boolean removeEntry(E entry) {
        boolean removed = children.remove(entry);
        if (removed && entry == getSelected()) setSelected(null);
        return removed;
    }

    protected @Nullable E getHovered() {
        return hovered;
    }

    void bindEntryToSelf(Entry<E> entry) {
        entry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput output, E entry) {
        List<E> entries = children();
        int index = entries.indexOf(entry);
        if (entries.size() > 1 && index != -1) {
            output.add(NarratedElementType.POSITION, Component.translatable("narrator.position.list", index + 1, entries.size()));
        }
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * 1.20.1's {@code AbstractSelectionList.Entry}: a row, drawn by {@code renderContent} (1.20.1 {@code render}) with
     * its index and bounds. It takes 1.20.1-shaped input from its list; 26.3 events reaching it go to those.
     */
    public abstract static class Entry<E extends Entry<E>> implements GuiEventListener {
        @Deprecated
        LegacySelectionList<E> list;

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return list.getFocused() == this;
        }

        /** 1.20.1 {@code Entry.render(graphics, index, top, left, width, height, mouseX, mouseY, hovered, partialTick)}. */
        public abstract void renderContent(GuiGraphicsExtractor graphics, int index, int top, int left, int width, int height,
                                           int mouseX, int mouseY, boolean hovered, float partialTick);

        public void renderBack(GuiGraphicsExtractor graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partialTick) {
        }

        @Override
        public boolean isMouseOver(double x, double y) {
            return Objects.equals(list.getEntryAtPosition(x, y), this);
        }

        public boolean mouseClicked(double x, double y, int button) {
            return false;
        }

        public boolean mouseReleased(double x, double y, int button) {
            return false;
        }

        public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
            return false;
        }

        public boolean mouseScrolled(double x, double y, double delta) {
            return false;
        }

        public boolean keyPressed(int key, int scancode, int modifiers) {
            return false;
        }

        public boolean keyReleased(int key, int scancode, int modifiers) {
            return false;
        }

        public boolean charTyped(char c, int modifiers) {
            return false;
        }

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

    /** The entries; adding one binds it to this list (its {@code list} field), as 1.20.1 did. */
    private class TrackedList extends AbstractList<E> {
        private final List<E> delegate = new ArrayList<>();

        @Override
        public E get(int index) {
            return delegate.get(index);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public E set(int index, E entry) {
            E old = delegate.set(index, entry);
            bindEntryToSelf(entry);
            return old;
        }

        @Override
        public void add(int index, E entry) {
            delegate.add(index, entry);
            bindEntryToSelf(entry);
        }

        @Override
        public E remove(int index) {
            return delegate.remove(index);
        }
    }
}

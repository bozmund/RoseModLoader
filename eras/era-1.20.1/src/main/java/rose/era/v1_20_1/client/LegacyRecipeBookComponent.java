package rose.era.v1_20_1.client;

import java.lang.invoke.MethodType;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jspecify.annotations.Nullable;
import rose.era.v1_20_1.bridge.Legacy;
import rose.era.v1_20_1.bridge.RecipeBookBridges;
import rose.era.v1_20_1.crafting.LegacyRecipeDisplay;

/**
 * 1.20.1 {@code RecipeBookComponent}, the recipe book of an old mod's screen, run by a 26.3 recipe book. Its tabs are
 * the ones the menu's mod registered (LegacyRecipeBooks), it shows the server's {@code rose:legacy} recipe displays,
 * ghost recipes go into the menu's crafting grid and clicks place recipes through the menu (RecipeBookBridges).
 * Mod subclasses are rebased onto this; their old hooks (filter textures, ghost setup) aren't called, the generic
 * grid placement stands in for them. A menu whose mod registered no tabs keeps the book hidden.
 */
public class LegacyRecipeBookComponent implements GuiEventListener, NarratableEntry {
    private static final Component ONLY_CRAFTABLE = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private @Nullable Book book;
    private boolean hidden;
    private boolean focused;

    public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow, RecipeBookMenu menu) {
        if (book == null || book.menu() != menu) {
            List<RecipeBookComponent.TabInfo> tabs = LegacyRecipeBooks.tabsFor(menu);
            book = tabs.isEmpty() ? null : new Book(menu, tabs, filterName());
        }
        if (book == null) return;
        book.init(width, height, minecraft, widthTooNarrow);
        if (hidden) book.show(false);
    }

    /** A mod's own {@code getRecipeFilterName()}, if it has one. */
    private Component filterName() {
        return Legacy.find(this, "getRecipeFilterName", MethodType.methodType(Component.class))
                .map(m -> (Component) Legacy.invoke(m, this)).orElse(ONLY_CRAFTABLE);
    }

    /** The screen's left position: next to the open book, or centered. */
    public int updateScreenPosition(int width, int imageWidth) {
        return book != null ? book.updateScreenPosition(width, imageWidth) : (width - imageWidth) / 2;
    }

    public boolean isVisible() {
        return book != null && book.isVisible();
    }

    public void setVisible(boolean visible) {
        if (book != null) book.show(visible);
    }

    public void toggleVisibility() {
        if (book != null) book.toggleVisibility();
    }

    /** 1.20.1 {@code hide()}: closes the book (mods call it when their config turns the book off). */
    public void hide() {
        hidden = true;
        if (book != null && book.isVisible()) book.show(false);
    }

    public void tick() {
        if (book != null) book.tick();
    }

    public void recipesUpdated() {
        if (book != null) book.recipesUpdated();
    }

    public void slotClicked(Slot slot) {
        if (book != null) book.slotClicked(slot);
    }

    /** 26.3's ghost recipe, sent by the server when a recipe can't be placed for lack of items. */
    public void fillGhostRecipe(RecipeDisplay display) {
        if (book != null) book.fillGhostRecipe(display);
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (book != null) book.extractRenderState(graphics, mouseX, mouseY, a);
    }

    /** 1.20.1 drew the ghost recipe from the screen with its position; 26.3 draws it inside the screen's slots pass. */
    public void renderGhostRecipe(GuiGraphicsExtractor graphics, int leftPos, int topPos, boolean bigResultSlot, float a) {
        if (book == null) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        book.extractGhostRecipe(graphics, bigResultSlot);
        graphics.pose().popMatrix();
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, int leftPos, int topPos, int mouseX, int mouseY) {
        if (book == null) return;
        Slot hovered = Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?> screen ? screen.hoveredSlot : null;
        book.extractTooltip(graphics, mouseX, mouseY, hovered);
    }

    /** 1.20.1 {@code mouseClicked(x, y, button)}, called by the screen first. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return book != null && book.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int imageWidth, int imageHeight, int button) {
        return book == null || book.hasClickedOutside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return book != null && book.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return book != null && book.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return book != null && book.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return book != null && book.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return book != null && book.charTyped(event);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return book != null && book.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (book != null) book.setFocused(focused);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return book != null ? book.narrationPriority() : NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        if (book != null) book.updateNarration(output);
    }

    /** The 26.3 book over a legacy menu's crafting grid. */
    private static final class Book extends RecipeBookComponent<RecipeBookMenu> {
        private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
                Identifier.withDefaultNamespace("recipe_book/filter_enabled"),
                Identifier.withDefaultNamespace("recipe_book/filter_disabled"),
                Identifier.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
                Identifier.withDefaultNamespace("recipe_book/filter_disabled_highlighted"));
        private final Component filterName;

        Book(RecipeBookMenu menu, List<TabInfo> tabs, Component filterName) {
            super(menu, tabs);
            this.filterName = filterName;
        }

        RecipeBookMenu menu() {
            return menu;
        }

        void show(boolean visible) {
            setVisible(visible);
        }

        @Override
        protected WidgetSprites getFilterButtonTextures() {
            return FILTER_BUTTON_SPRITES;
        }

        @Override
        protected boolean isCraftingSlot(Slot slot) {
            return RecipeBookBridges.grid(menu).contains(slot);
        }

        @Override
        protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
            collection.selectRecipes(stackedContents, display -> display instanceof LegacyRecipeDisplay);
        }

        @Override
        protected Component getRecipeFilterName() {
            return filterName;
        }

        @Override
        protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipe, ContextMap context) {
            if (!(recipe instanceof LegacyRecipeDisplay legacy)) return;
            int result = RecipeBookBridges.resultSlot(menu);
            if (result >= 0 && result < menu.slots.size()) ghostSlots.setResult(menu.getSlot(result), context, legacy.result());
            List<Slot> grid = RecipeBookBridges.grid(menu);
            for (int i = 0; i < Math.min(grid.size(), legacy.ingredients().size()); i++) {
                ghostSlots.setInput(grid.get(i), context, legacy.ingredients().get(i));
            }
        }
    }
}

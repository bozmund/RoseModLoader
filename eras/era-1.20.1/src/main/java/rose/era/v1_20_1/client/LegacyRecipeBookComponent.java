package rose.era.v1_20_1.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;

/**
 * 1.20.1 {@code RecipeBookComponent} as an always-hidden recipe book. 1.21.2 rebuilt the recipe book around
 * server-side recipe displays, which old mods' recipe book subclasses can't drive; their screens keep working
 * without it. Mod subclasses are rebased onto this.
 */
public class LegacyRecipeBookComponent implements GuiEventListener, NarratableEntry {
    private boolean focused;

    public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow, RecipeBookMenu menu) {
    }

    /** The screen's left position: centered, as with the book closed. */
    public int updateScreenPosition(int width, int imageWidth) {
        return (width - imageWidth) / 2;
    }

    public boolean isVisible() {
        return false;
    }

    public void setVisible(boolean visible) {
    }

    public void toggleVisibility() {
    }

    public void hide() {
    }

    public void tick() {
    }

    public void recipesUpdated() {
    }

    public void slotClicked(Slot slot) {
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    public void renderGhostRecipe(GuiGraphicsExtractor graphics, int leftPos, int topPos, boolean big, float a) {
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, int leftPos, int topPos, int mouseX, int mouseY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, int imageWidth, int imageHeight, int button) {
        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }
}

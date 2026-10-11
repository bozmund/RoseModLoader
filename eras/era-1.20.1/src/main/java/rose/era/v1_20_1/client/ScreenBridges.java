package rose.era.v1_20_1.client;

import java.lang.invoke.MethodType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import rose.era.v1_20_1.bridge.Legacy;

/** Bridge helpers for 1.20.1 screens (rules/forge-1.20.1/bridges.tsv). */
public final class ScreenBridges {
    private static final MethodType EXTRACT_BACKGROUND = MethodType.methodType(void.class, GuiGraphicsExtractor.class, int.class, int.class, float.class);
    private static final MethodType RENDER_BG = MethodType.methodType(void.class, GuiGraphicsExtractor.class, float.class, int.class, int.class);

    /**
     * 26.3 draws a screen's background in {@code extractBackground} (before the screen's contents); 1.20.1 container
     * screens drew their texture in {@code renderBg(graphics, partialTick, mouseX, mouseY)}.
     */
    public static void extractBackground(AbstractContainerScreen<?> self, GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Legacy.invoke(Legacy.superMethod(self, "extractBackground", EXTRACT_BACKGROUND), self, g, mouseX, mouseY, a);
        Legacy.invoke(Legacy.require(self, "renderBg", RENDER_BG), self, g, a, mouseX, mouseY);
    }

    /**
     * 1.21.2 {@code RecipeUpdateListener.fillGhostRecipe}: the server's ghost recipe goes to the screen's recipe book
     * (1.20.1 {@code getRecipeBookComponent()}).
     */
    public static void fillGhostRecipe(RecipeUpdateListener self, RecipeDisplay display) {
        Legacy.find(self, "getRecipeBookComponent", MethodType.methodType(LegacyRecipeBookComponent.class))
                .map(m -> (LegacyRecipeBookComponent) Legacy.invoke(m, self))
                .ifPresent(book -> book.fillGhostRecipe(display));
    }

    private ScreenBridges() {}
}

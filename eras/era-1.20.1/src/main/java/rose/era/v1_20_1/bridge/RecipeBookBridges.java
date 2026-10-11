package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Bridge helpers that make a 1.20.1 {@code RecipeBookMenu} a 26.3 one. 1.20.1 menus described their crafting grid
 * (getGridWidth/getGridHeight, slots 0 to width*height-1) and placed recipes through ServerPlaceRecipe; 26.3 menus
 * place them themselves in {@code handlePlacement}, which here drives 26.3's ServerPlaceRecipe with that grid.
 */
public final class RecipeBookBridges {
    public static RecipeBookMenu.PostPlaceAction handlePlacement(RecipeBookMenu self, boolean useMaxItems, boolean allowDroppingItemsToClear,
                                                                 RecipeHolder<?> recipe, ServerLevel level, Inventory inventory) {
        int width = gridWidth(self);
        int height = gridHeight(self);
        List<Slot> grid = grid(self);
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeHolder<Recipe<RecipeInput>> holder = (RecipeHolder) recipe;
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<Recipe<RecipeInput>>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents contents) {
                RecipeBookBridges.fillCraftSlotsStackedContents(self, contents);
            }

            @Override
            public void clearCraftingContent() {
                var old = Legacy.find(self, "clearCraftingContent", MethodType.methodType(void.class));
                if (old.isPresent()) Legacy.invoke(old.get(), self);
                else grid.forEach(slot -> slot.set(ItemStack.EMPTY));
            }

            @Override
            public boolean recipeMatches(RecipeHolder<Recipe<RecipeInput>> placed) {
                return Legacy.find(self, "recipeMatches", MethodType.methodType(boolean.class, Recipe.class))
                        .map(m -> (Boolean) Legacy.invoke(m, self, placed.value())).orElse(false);
            }
        }, width, height, grid, grid, inventory, holder, useMaxItems, allowDroppingItemsToClear);
    }

    /** The items already in the crafting grid count towards what can be crafted. */
    public static void fillCraftSlotsStackedContents(RecipeBookMenu self, StackedItemContents contents) {
        for (Slot slot : grid(self)) contents.accountSimpleStack(slot.getItem());
    }

    /** The menu's crafting grid: slots 0 to width*height-1, as 1.20.1's recipe book placed them. */
    public static List<Slot> grid(RecipeBookMenu self) {
        int size = Math.min(gridWidth(self) * gridHeight(self), self.slots.size());
        return IntStream.range(0, size).mapToObj(self::getSlot).toList();
    }

    public static int gridWidth(RecipeBookMenu self) {
        return legacyInt(self, "getGridWidth");
    }

    public static int gridHeight(RecipeBookMenu self) {
        return legacyInt(self, "getGridHeight");
    }

    /** 1.20.1 {@code getResultSlotIndex()}, or -1. */
    public static int resultSlot(RecipeBookMenu self) {
        return Legacy.find(self, "getResultSlotIndex", MethodType.methodType(int.class)).map(m -> (Integer) Legacy.invoke(m, self)).orElse(-1);
    }

    private static int legacyInt(RecipeBookMenu self, String name) {
        return Legacy.find(self, name, MethodType.methodType(int.class)).map(m -> (Integer) Legacy.invoke(m, self)).orElse(1);
    }

    private RecipeBookBridges() {}
}

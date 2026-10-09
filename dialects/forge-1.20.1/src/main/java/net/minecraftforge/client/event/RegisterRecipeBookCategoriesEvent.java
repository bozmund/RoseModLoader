package net.minecraftforge.client.event;

import java.util.List;
import java.util.function.Function;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.dialect.forge.v1_20_1.Unsupported;

/**
 * Client recipe book categories. 26.3 recipe books are driven by the server (recipe display data), so these
 * registrations have nothing to configure and are recorded only.
 */
public class RegisterRecipeBookCategoriesEvent extends Event implements IModBusEvent {
    public void registerBookCategories(RecipeBookType type, List<RecipeBookCategories> categories) {
        Unsupported.feature("recipe-book-categories", "client recipe book categories are server-driven on 26.3; registration ignored");
    }

    public void registerAggregateCategory(RecipeBookCategories category, List<RecipeBookCategories> others) {
        Unsupported.feature("recipe-book-categories", "client recipe book categories are server-driven on 26.3; registration ignored");
    }

    public void registerRecipeCategoryFinder(RecipeType<?> type, Function<?, RecipeBookCategories> lookup) {
        Unsupported.feature("recipe-book-categories", "client recipe book categories are server-driven on 26.3; registration ignored");
    }
}

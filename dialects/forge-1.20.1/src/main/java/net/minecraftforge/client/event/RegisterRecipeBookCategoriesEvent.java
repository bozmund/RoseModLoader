package net.minecraftforge.client.event;

import java.util.List;
import java.util.function.Function;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.era.v1_20_1.client.LegacyRecipeBookCategory;
import rose.era.v1_20_1.client.LegacyRecipeBooks;

/**
 * Client recipe book categories: the tabs of a mod's recipe books and how its recipes sort into them. Rose's legacy
 * recipe book uses them (LegacyRecipeBooks). Custom book types share one 26.3 type (RecipeBookTypeShim), so a menu's
 * book shows the tabs its own mod registered.
 */
public class RegisterRecipeBookCategoriesEvent extends Event implements IModBusEvent {
    public void registerBookCategories(RecipeBookType type, List<LegacyRecipeBookCategory> categories) {
        var mod = ForgeDialect.activeMod();
        if (mod != null) LegacyRecipeBooks.registerBookCategories(mod.modId(), categories);
    }

    public void registerAggregateCategory(LegacyRecipeBookCategory category, List<LegacyRecipeBookCategory> others) {
        LegacyRecipeBooks.registerAggregate(category, others);
    }

    public void registerRecipeCategoryFinder(RecipeType<?> type, Function<?, LegacyRecipeBookCategory> lookup) {
        LegacyRecipeBooks.registerFinder(type, lookup);
    }
}

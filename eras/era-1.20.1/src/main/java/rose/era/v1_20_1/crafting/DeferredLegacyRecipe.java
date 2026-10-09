package rose.era.v1_20_1.crafting;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Stands in for a 1.20.1 recipe while recipes load. Old serializers build item stacks while parsing, and 26.3 binds
 * item components only after the recipe registry is loaded, so the real recipe is created later
 * ({@link LegacyRecipes#materialize}, when the recipe manager finalizes loading).
 */
public final class DeferredLegacyRecipe implements Recipe<RecipeInput> {
    /** The type deferred recipes are indexed under until they're replaced. */
    public static final RecipeType<DeferredLegacyRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "rose:deferred_legacy";
        }
    };

    final LegacyRecipeSerializer<?> serializer;
    final Identifier id;
    final JsonObject json;

    DeferredLegacyRecipe(LegacyRecipeSerializer<?> serializer, Identifier id, JsonObject json) {
        this.serializer = serializer;
        this.id = id;
        this.json = json;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return (RecipeSerializer) LegacyRecipes.adapted(serializer);
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}

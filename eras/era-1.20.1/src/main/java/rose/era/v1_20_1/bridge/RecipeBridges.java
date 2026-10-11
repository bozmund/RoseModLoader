package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import rose.era.v1_20_1.EraContext;
import rose.era.v1_20_1.crafting.LegacyRecipeSerializer;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/**
 * Bridge helpers that make a 1.20.1 recipe class a 26.3 {@link Recipe}: 26.3 recipes match a RecipeInput (1.20.1:
 * a Container), assemble without a registry access, and describe their recipe book placement.
 */
public final class RecipeBridges {
    public static boolean matches(Recipe<?> self, RecipeInput input, Level level) {
        var old = Legacy.require(self, "matches", MethodType.methodType(boolean.class, Container.class, Level.class));
        return (Boolean) Legacy.invoke(old, self, LegacyContainers.asContainer(input), level);
    }

    public static ItemStack assemble(Recipe<?> self, RecipeInput input) {
        var old = Legacy.require(self, "assemble", MethodType.methodType(ItemStack.class, Container.class, RegistryAccess.class));
        return (ItemStack) Legacy.invoke(old, self, LegacyContainers.asContainer(input), EraContext.registryAccess());
    }

    public static RecipeSerializer<?> getSerializer(Recipe<?> self) {
        var old = Legacy.require(self, "getSerializer", MethodType.methodType(LegacyRecipeSerializer.class));
        LegacyRecipeSerializer<?> legacy = (LegacyRecipeSerializer<?>) Legacy.invoke(old, self);
        RecipeSerializer<?> adapted = LegacyRecipes.adapted(legacy);
        if (adapted == null) throw new IllegalStateException("Recipe serializer " + legacy + " of " + self.getClass().getName() + " was never registered");
        return adapted;
    }

    /** 1.20.1 {@code getGroup()} defaulted to "". */
    public static String group(Recipe<?> self) {
        return Legacy.find(self, "getGroup", MethodType.methodType(String.class)).map(m -> (String) Legacy.invoke(m, self)).orElse("");
    }

    /** 1.20.1 recipes showed a toast by default. */
    public static boolean showNotification(Recipe<?> self) {
        return true;
    }

    /**
     * The recipe's inputs, from 1.20.1 {@code getIngredients()} (default: none). 26.3 ignores a non-special recipe
     * without them ("can't be placed due to empty ingredients"). Empty ingredients are shaped-recipe gaps; the
     * recipe book can't place 1.20.1 recipes slot by slot anyway, so only the inputs are kept.
     */
    @SuppressWarnings("unchecked")
    public static PlacementInfo placementInfo(Recipe<?> self) {
        List<Ingredient> ingredients = Legacy.find(self, "getIngredients", MethodType.methodType(NonNullList.class))
                .map(m -> (List<Ingredient>) Legacy.invoke(m, self))
                .orElse(List.of());
        List<Ingredient> present = ingredients.stream().filter(i -> !i.isEmpty()).toList();
        return present.isEmpty() ? PlacementInfo.NOT_PLACEABLE : PlacementInfo.create(present);
    }

    /** Mod recipe types go to the legacy book category (sorted into the mod's tabs on the client), others to crafting. */
    public static RecipeBookCategory recipeBookCategory(Recipe<?> self) {
        return isModType(self) ? rose.era.v1_20_1.crafting.LegacyRecipeDisplay.CATEGORY : RecipeBookCategories.CRAFTING_MISC;
    }

    /**
     * The recipe book display of a recipe of a mod recipe type: its inputs and result, and its source for the client
     * (see LegacyRecipeDisplay). Recipes of vanilla types have none: they'd appear in vanilla's books, which can't
     * place them.
     */
    @SuppressWarnings("unchecked")
    public static List<net.minecraft.world.item.crafting.display.RecipeDisplay> display(Recipe<?> self) {
        var json = LegacyRecipes.sourceOf(self);
        if (json == null || !isModType(self)) return List.of();
        List<net.minecraft.world.item.crafting.display.SlotDisplay> inputs = Legacy.find(self, "getIngredients", MethodType.methodType(NonNullList.class))
                .map(m -> (List<Ingredient>) Legacy.invoke(m, self)).orElse(List.of())
                .stream().filter(i -> !i.isEmpty()).map(Ingredient::display).toList();
        ItemStack result = Legacy.find(self, "getResultItem", MethodType.methodType(ItemStack.class, RegistryAccess.class))
                .map(m -> (ItemStack) Legacy.invoke(m, self, EraContext.registryAccess())).orElse(ItemStack.EMPTY);
        // 1.20.1 getToastSymbol(): the recipe's workstation icon (a crafting table unless the mod says otherwise)
        ItemStack station = Legacy.find(self, "getToastSymbol", MethodType.methodType(ItemStack.class))
                .map(m -> (ItemStack) Legacy.invoke(m, self)).orElse(ItemStack.EMPTY);
        var serializer = net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER.getKey(self.getSerializer());
        var id = rose.era.v1_20_1.shim.RecipeShim.getId(self);
        if (serializer == null || id == null) return List.of();
        return List.of(new rose.era.v1_20_1.crafting.LegacyRecipeDisplay(inputs,
                result.isEmpty() ? net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE
                        : new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(result)),
                station.isEmpty() ? net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE
                        : new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(station.typeHolder()),
                serializer, id, json.toString()));
    }

    private static boolean isModType(Recipe<?> self) {
        var type = net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE.getKey(self.getType());
        return type != null && !type.getNamespace().equals("minecraft");
    }

    private RecipeBridges() {}
}

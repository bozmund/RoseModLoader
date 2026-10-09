package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Locale;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import rose.era.v1_20_1.crafting.LegacyRecipeSerializer;

/** Era bridge (1.20.1): serializer for special crafting recipes that only have a book category. */
public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements LegacyRecipeSerializer<T> {
    private final Factory<T> constructor;

    public SimpleCraftingRecipeSerializer(Factory<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T fromJson(Identifier recipeId, JsonObject json) {
        CraftingBookCategory category = CraftingBookCategory.MISC;
        if (json.has("category")) {
            String name = json.get("category").getAsString();
            for (CraftingBookCategory c : CraftingBookCategory.values()) {
                if (c.getSerializedName().equals(name.toLowerCase(Locale.ROOT))) category = c;
            }
        }
        return constructor.create(recipeId, category);
    }

    @Override
    public T fromNetwork(Identifier recipeId, FriendlyByteBuf buffer) {
        return constructor.create(recipeId, buffer.readEnum(CraftingBookCategory.class));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        buffer.writeEnum(recipe.category());
    }

    @FunctionalInterface
    public interface Factory<T extends CraftingRecipe> {
        T create(Identifier id, CraftingBookCategory category);
    }
}

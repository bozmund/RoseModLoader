package rose.era.v1_20_1.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Era bridge (1.20.1): the old {@code RecipeSerializer} interface (JSON and packet buffer). 26.3 made
 * RecipeSerializer a record of codecs, so translated mods implement this instead, and {@link LegacyRecipes#adapt}
 * turns each one into a 26.3 serializer when it is registered.
 */
public interface LegacyRecipeSerializer<T extends Recipe<?>> {
    T fromJson(Identifier recipeId, JsonObject json);

    T fromNetwork(Identifier recipeId, FriendlyByteBuf buffer);

    void toNetwork(FriendlyByteBuf buffer, T recipe);
}

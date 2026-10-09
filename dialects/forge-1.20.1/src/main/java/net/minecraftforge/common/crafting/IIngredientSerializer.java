package net.minecraftforge.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

/** Reads and writes a custom ingredient type (Forge 1.20.1 recipes were JSON). */
public interface IIngredientSerializer<T extends Ingredient> {
    T parse(FriendlyByteBuf buffer);

    T parse(JsonObject json);

    void write(FriendlyByteBuf buffer, T ingredient);
}

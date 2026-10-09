package net.minecraftforge.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/** Forge recipe helpers: registries of custom conditions and ingredient types, and JSON item stacks. */
public final class CraftingHelper {
    private static final Map<Identifier, IConditionSerializer<?>> CONDITIONS = new ConcurrentHashMap<>();
    private static final Map<Identifier, IIngredientSerializer<?>> INGREDIENTS = new ConcurrentHashMap<>();

    public static IConditionSerializer<?> register(IConditionSerializer<?> serializer) {
        if (CONDITIONS.putIfAbsent(serializer.getID(), serializer) != null) {
            throw new IllegalStateException("Duplicate recipe condition serializer: " + serializer.getID());
        }
        return serializer;
    }

    public static <T extends net.minecraft.world.item.crafting.Ingredient> IIngredientSerializer<T> register(Identifier key, IIngredientSerializer<T> serializer) {
        if (INGREDIENTS.putIfAbsent(key, serializer) != null) throw new IllegalStateException("Duplicate ingredient serializer: " + key);
        rose.era.v1_20_1.shim.IngredientShim.registerCustom(key.toString(), serializer::parse);
        return serializer;
    }

    public static Identifier getID(IIngredientSerializer<?> serializer) {
        for (var e : INGREDIENTS.entrySet()) if (e.getValue() == serializer) return e.getKey();
        return null;
    }

    public static IConditionSerializer<?> conditionSerializer(Identifier id) {
        return CONDITIONS.get(id);
    }

    /** {@code {"item": "minecraft:stone", "count": 2}}. NBT ("nbt") from 1.20.1 data is not applied (26.3 uses components). */
    public static ItemStack getItemStack(JsonObject json, boolean readNBT) {
        String id = json.get("item").getAsString();
        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id))
                .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + id + "'"));
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        return new ItemStack(item, count);
    }

    public static boolean processConditions(JsonObject json, String memberName, ICondition.IContext context) {
        return true;
    }

    private CraftingHelper() {}
}

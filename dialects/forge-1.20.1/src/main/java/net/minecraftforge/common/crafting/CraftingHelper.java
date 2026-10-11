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

    /** Whether {@code json.memberName} (a list of conditions) is met; no list means no conditions. */
    public static boolean processConditions(JsonObject json, String memberName, ICondition.IContext context) {
        return !json.has(memberName) || processConditions(json.getAsJsonArray(memberName), context);
    }

    /** Whether every condition in {@code conditions} is met (Forge's built-in types, then registered serializers). */
    public static boolean processConditions(com.google.gson.JsonArray conditions, ICondition.IContext context) {
        for (var element : conditions) {
            if (!element.isJsonObject()) throw new JsonSyntaxException("Conditions must be an array of JsonObjects");
            if (!test(element.getAsJsonObject(), context)) return false;
        }
        return true;
    }

    private static boolean test(JsonObject condition, ICondition.IContext context) {
        String type = condition.has("type") ? condition.get("type").getAsString() : "";
        switch (type) {
            case "forge:true":
                return true;
            case "forge:false":
                return false;
            case "forge:mod_loaded":
                return net.minecraftforge.fml.ModList.get().isLoaded(condition.get("modid").getAsString());
            case "forge:item_exists":
                return net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Identifier.parse(condition.get("item").getAsString()));
            case "forge:not":
                return !test(condition.getAsJsonObject("value"), context);
            case "forge:and":
                return processConditions(condition.getAsJsonArray("values"), context);
            case "forge:or":
                for (var value : condition.getAsJsonArray("values")) if (test(value.getAsJsonObject(), context)) return true;
                return false;
            case "forge:tag_empty": {
                var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(condition.get("tag").getAsString()));
                return context.getTag(tag).isEmpty();
            }
            default: {
                IConditionSerializer<?> serializer = conditionSerializer(Identifier.parse(type));
                if (serializer == null) throw new JsonSyntaxException("Unknown condition type: " + type);
                return serializer.read(condition).test(context);
            }
        }
    }

    private CraftingHelper() {}
}

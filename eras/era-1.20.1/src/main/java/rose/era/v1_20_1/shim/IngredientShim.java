package rose.era.v1_20_1.shim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Ingredient;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/** Redirect targets for 1.20.1 {@code Ingredient} JSON reading (1.21.2 changed the format and removed fromJson). */
public final class IngredientShim {
    private static final java.util.Map<String, java.util.function.Function<JsonObject, Ingredient>> CUSTOM =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** A custom ingredient type ({@code {"type": id, ...}}), e.g. registered through Forge's CraftingHelper. */
    public static void registerCustom(String type, java.util.function.Function<JsonObject, Ingredient> parser) {
        CUSTOM.put(type, parser);
    }

    /**
     * 1.20.1 {@code Ingredient.fromJson(json)}: {@code {"item": id}}, {@code {"tag": tag}} or a list of those. Converted
     * to the 26.3 format and read with vanilla's codec, using the registry context of the recipe being loaded.
     */
    public static Ingredient fromJson(JsonElement json) {
        // A one-element list (a common way to write a single ingredient) is that ingredient.
        if (json.isJsonArray() && json.getAsJsonArray().size() == 1) return fromJson(json.getAsJsonArray().get(0));
        // A list with tags or custom types can't become one HolderSet (26.3 lists hold item ids only): match any of
        // its parts instead.
        if (json.isJsonArray() && needsParts(json.getAsJsonArray())) {
            java.util.List<Ingredient> parts = new java.util.ArrayList<>();
            for (JsonElement e : json.getAsJsonArray()) parts.add(fromJson(e));
            return new rose.era.v1_20_1.crafting.AnyOfIngredient(parts);
        }
        if (json.isJsonObject() && json.getAsJsonObject().has("type")) {
            var parser = CUSTOM.get(json.getAsJsonObject().get("type").getAsString());
            if (parser != null) return parser.apply(json.getAsJsonObject());
        }
        DynamicOps<JsonElement> ops = LegacyRecipes.currentOps();
        return Ingredient.CODEC.parse(ops != null ? ops : JsonOps.INSTANCE, upgrade(json))
                .getOrThrow(message -> new JsonParseException("Bad 1.20.1 ingredient " + json + ": " + message));
    }

    private static boolean needsParts(JsonArray array) {
        for (JsonElement e : array) {
            if (e.isJsonObject() && (e.getAsJsonObject().has("type") || e.getAsJsonObject().has("tag"))) return true;
            if (e.isJsonArray()) return true;
        }
        return false;
    }

    /** 1.20.1 {@code Ingredient.fromJson(json, allowEmpty)}. */
    public static Ingredient fromJson(JsonElement json, boolean allowEmpty) {
        return fromJson(json);
    }

    /** 1.20.1 {@code Ingredient.of(ItemStack...)}: matches the stacks' items (26.3 ingredients don't look at counts). */
    public static Ingredient ofStacks(net.minecraft.world.item.ItemStack[] stacks) {
        return Ingredient.of(java.util.Arrays.stream(stacks).map(net.minecraft.world.item.ItemStack::getItem));
    }

    /** 1.20.1 ingredient JSON in 1.21.2 form: "id", "#tag" or a list of ids (lists with tags never get here). */
    static JsonElement upgrade(JsonElement old) {
        if (old.isJsonArray()) {
            JsonArray ids = new JsonArray();
            for (JsonElement e : old.getAsJsonArray()) ids.add(upgrade(e));
            return ids.size() == 1 ? ids.get(0) : ids;
        }
        if (!old.isJsonObject()) return old;
        JsonObject o = old.getAsJsonObject();
        if (o.has("item")) return o.get("item");
        if (o.has("tag")) return new JsonPrimitive("#" + o.get("tag").getAsString());
        throw new JsonParseException("Unsupported 1.20.1 ingredient type " + o);
    }

    private IngredientShim() {}
}

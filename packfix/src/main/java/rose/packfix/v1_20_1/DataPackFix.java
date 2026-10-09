package rose.packfix.v1_20_1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Upgrades a 1.20.1 mod's data pack files to what 26.3 reads, when the mod jar is translated:
 * <ul>
 *   <li>folder names: 1.21 made data pack folders singular ({@code recipes} to {@code recipe}, {@code tags/items} to
 *       {@code tags/item}, ...);</li>
 *   <li>vanilla recipe types: 1.21.2 ingredients are ids/tags as strings, results are {@code {"id", "count"}};
 *       Forge recipe conditions are resolved (other mods' integration recipes are left out);</li>
 *   <li>worldgen: dropped until the worldgen era bridge exists (26.x restructured features; one unreadable worldgen
 *       file stops a world from loading).</li>
 * </ul>
 * Everything changed or dropped is reported through {@link #report()}.
 */
public final class DataPackFix {
    /** Data folders that 1.21 renamed (24w21a: "data pack directories are now singular"). */
    static final Map<String, String> FOLDERS = Map.ofEntries(
            Map.entry("recipes", "recipe"),
            Map.entry("loot_tables", "loot_table"),
            Map.entry("advancements", "advancement"),
            Map.entry("structures", "structure"),
            Map.entry("predicates", "predicate"),
            Map.entry("item_modifiers", "item_modifier"),
            Map.entry("functions", "function"),
            Map.entry("tags/blocks", "tags/block"),
            Map.entry("tags/items", "tags/item"),
            Map.entry("tags/entity_types", "tags/entity_type"),
            Map.entry("tags/fluids", "tags/fluid"),
            Map.entry("tags/game_events", "tags/game_event"),
            Map.entry("tags/functions", "tags/function"));
    /** Criterion fields that were 1.20.1 ContextAwarePredicates (a condition list, or a bare entity predicate). */
    private static final Set<String> ENTITY_CONTEXT_KEYS = Set.of("player", "entity", "child", "parent", "partner", "zombie",
            "villager", "projectile", "shooter", "lightning", "bystander", "source");
    private static final Pattern DAMAGE_NBT = Pattern.compile("\\{\\s*Damage\\s*:\\s*(\\d+)\\s*}");
    private static final Pattern DATA_PATH = Pattern.compile("data/([^/]+)/(.+)");
    private static final Set<String> COOKING = Set.of("minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
            "minecraft:campfire_cooking");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** The new path and content of a resource; content is {@code null} if unchanged. */
    public record Fixed(String path, byte[] content) {}

    private final Set<String> knownMods;
    private final List<String> report = new ArrayList<>();

    /** @param knownMods mod ids whose integration data should be kept (the mod itself, "forge", "minecraft") */
    public DataPackFix(Set<String> knownMods) {
        this.knownMods = Set.copyOf(knownMods);
    }

    public List<String> report() {
        return List.copyOf(report);
    }

    /** The fixed resource, or {@code null} to leave it out of the translated jar. */
    public Fixed fix(String path, byte[] content) {
        Matcher m = DATA_PATH.matcher(path);
        if (!m.matches()) return new Fixed(path, null);
        String namespace = m.group(1);
        String rest = m.group(2);
        if (rest.startsWith("worldgen/configured_feature/") || rest.startsWith("worldgen/placed_feature/")) {
            JsonObject feature = parse(content);
            if (feature == null) return null;
            if (feature.has("type") && WorldgenFix.UNSUPPORTED_FEATURES.contains(feature.get("type").getAsString())) {
                report.add("dropped " + path + " (" + feature.get("type").getAsString() + " not bridged yet)");
                return null;
            }
            String folder = rest.replaceFirst("^worldgen/configured_feature/", "worldgen/feature/");
            return new Fixed("data/" + namespace + "/" + folder, GSON.toJson(WorldgenFix.upgrade(feature)).getBytes(StandardCharsets.UTF_8));
        }
        if (rest.startsWith("worldgen/")) {
            report.add("dropped " + path + " (worldgen not bridged yet)");
            return null;
        }
        String folder = rest;
        for (var e : FOLDERS.entrySet()) {
            if (rest.startsWith(e.getKey() + "/")) {
                folder = e.getValue() + rest.substring(e.getKey().length());
                break;
            }
        }
        String newPath = "data/" + namespace + "/" + folder;
        if (folder.startsWith("tags/") && path.endsWith(".json")) {
            JsonObject tag = parse(content);
            if (tag == null) return new Fixed(newPath, null);
            return new Fixed(newPath, GSON.toJson(optionalEntries(tag)).getBytes(StandardCharsets.UTF_8));
        }
        if (folder.startsWith("recipe/") && path.endsWith(".json")) {
            JsonObject recipe = parse(content);
            if (recipe == null) return new Fixed(newPath, null);
            JsonObject upgraded = upgradeRecipe(recipe, path);
            if (upgraded == null) return null;
            return new Fixed(newPath, GSON.toJson(upgraded).getBytes(StandardCharsets.UTF_8));
        }
        if (folder.startsWith("loot_table/") && path.endsWith(".json")) {
            JsonObject table = parse(content);
            if (table == null) return new Fixed(newPath, null);
            return new Fixed(newPath, GSON.toJson(LootTableFix.table(table)).getBytes(StandardCharsets.UTF_8));
        }
        if (folder.startsWith("advancement/") && path.endsWith(".json")) {
            JsonObject advancement = parse(content);
            if (advancement == null) return new Fixed(newPath, null);
            JsonObject upgraded = upgradeAdvancement(advancement, path);
            if (upgraded == null) return null;
            return new Fixed(newPath, GSON.toJson(upgraded).getBytes(StandardCharsets.UTF_8));
        }
        return new Fixed(newPath, null);
    }

    /** Parses a JSON object, with vanilla ids renamed since 1.20.1 already applied. */
    private static JsonObject parse(byte[] content) {
        try {
            JsonElement e = JsonParser.parseString(new String(content, StandardCharsets.UTF_8));
            return e.isJsonObject() ? VanillaIds.rename(e).getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Every entry of an old mod's tag becomes optional ({@code {"id": x, "required": false}}): an entry for content
     * that didn't load (or another mod's) would otherwise fail the whole tag, including vanilla's tag it adds to.
     */
    static JsonObject optionalEntries(JsonObject tag) {
        JsonArray values = tag.getAsJsonArray("values");
        if (values == null) return tag;
        JsonArray out = new JsonArray();
        for (JsonElement v : values) {
            if (v.isJsonPrimitive()) {
                JsonObject entry = new JsonObject();
                entry.add("id", v);
                entry.addProperty("required", false);
                out.add(entry);
            } else {
                JsonObject entry = v.getAsJsonObject();
                entry.addProperty("required", false);
                out.add(entry);
            }
        }
        tag.add("values", out);
        return tag;
    }

    /** A 26.3 recipe, or {@code null} if it shouldn't load (conditions not met). */
    JsonObject upgradeRecipe(JsonObject recipe, String path) {
        String type = recipe.has("type") ? recipe.get("type").getAsString() : "";
        if (type.equals("forge:conditional")) {
            for (JsonElement option : recipe.getAsJsonArray("recipes")) {
                JsonObject o = option.getAsJsonObject();
                if (conditionsMet(o.getAsJsonArray("conditions"), path)) return upgradeRecipe(o.getAsJsonObject("recipe"), path);
            }
            report.add("dropped " + path + " (no conditional branch applies)");
            return null;
        }
        if (recipe.has("conditions")) {
            if (!conditionsMet(recipe.getAsJsonArray("conditions"), path)) {
                report.add("dropped " + path + " (conditions: needs another mod)");
                return null;
            }
            recipe.remove("conditions");
        }
        switch (type) {
            case "minecraft:crafting_shaped" -> {
                JsonObject key = recipe.getAsJsonObject("key");
                if (key != null) for (String k : List.copyOf(key.keySet())) key.add(k, ingredient(key.get(k)));
                upgradeResult(recipe);
            }
            case "minecraft:crafting_shapeless" -> {
                JsonArray ingredients = recipe.getAsJsonArray("ingredients");
                JsonArray out = new JsonArray();
                if (ingredients != null) for (JsonElement i : ingredients) out.add(ingredient(i));
                recipe.add("ingredients", out);
                upgradeResult(recipe);
            }
            case "minecraft:stonecutting" -> {
                recipe.add("ingredient", ingredient(recipe.get("ingredient")));
                JsonObject result = new JsonObject();
                result.add("id", recipe.get("result"));
                if (recipe.has("count")) result.add("count", recipe.remove("count"));
                recipe.add("result", result);
            }
            case "minecraft:smithing_transform" -> {
                for (String slot : List.of("template", "base", "addition")) {
                    if (recipe.has(slot)) recipe.add(slot, ingredient(recipe.get(slot)));
                }
                upgradeResult(recipe);
            }
            default -> {
                if (COOKING.contains(type)) {
                    recipe.add("ingredient", ingredient(recipe.get("ingredient")));
                    upgradeResult(recipe);
                }
            }
        }
        return recipe;
    }

    /** {@code {"item": x, "count": n}} or {@code "x"} becomes {@code {"id": x, "count": n}}. */
    private static void upgradeResult(JsonObject recipe) {
        JsonElement result = recipe.get("result");
        if (result == null) return;
        JsonObject out = new JsonObject();
        if (result.isJsonPrimitive()) {
            out.add("id", result);
        } else {
            JsonObject r = result.getAsJsonObject();
            out.add("id", r.has("item") ? r.get("item") : r.get("id"));
            if (r.has("count")) out.add("count", r.get("count"));
        }
        recipe.add("result", out);
    }

    /**
     * 1.20.1 ingredient JSON to 1.21.2: {@code {"item": x}} becomes {@code "x"}, {@code {"tag": t}} becomes {@code "#t"},
     * a list of items becomes a list of ids. Lists that mix tags and items can't be expressed; the first entry is kept.
     */
    static JsonElement ingredient(JsonElement old) {
        if (old == null) return null;
        if (old.isJsonArray()) {
            JsonArray items = new JsonArray();
            for (JsonElement e : old.getAsJsonArray()) {
                JsonElement converted = ingredient(e);
                if (converted.isJsonPrimitive() && converted.getAsString().startsWith("#")) return converted;
                items.add(converted);
            }
            return items.size() == 1 ? items.get(0) : items;
        }
        if (!old.isJsonObject()) return old;
        JsonObject o = old.getAsJsonObject();
        if (o.has("item")) return o.get("item");
        if (o.has("tag")) return new com.google.gson.JsonPrimitive("#" + o.get("tag").getAsString());
        return old;
    }

    /** Forge 1.20.1 conditions; other mods count as absent (the translated jar is cached without the mod list). */
    /**
     * A 26.3 advancement, or {@code null} if it shouldn't load. Changes since 1.20.1 (26.3 codecs): Forge conditions
     * are resolved; the icon is an item stack template ({@code id}); criterion player/location/entity predicates are
     * one loot condition, not a list; {@code recipe_unlocked} takes {@code recipes}; item predicates name tags in
     * {@code items} ({@code #tag}) and match NBT through components.
     */
    JsonObject upgradeAdvancement(JsonObject advancement, String path) {
        if (advancement.has("advancements") && advancement.get("advancements").isJsonArray()) {
            for (JsonElement option : advancement.getAsJsonArray("advancements")) {
                JsonObject o = option.getAsJsonObject();
                if (conditionsMet(o.getAsJsonArray("conditions"), path)) return upgradeAdvancement(o.getAsJsonObject("advancement"), path);
            }
            report.add("dropped " + path + " (no conditional branch applies)");
            return null;
        }
        if (advancement.has("conditions") && advancement.get("conditions").isJsonArray()) {
            if (!conditionsMet(advancement.getAsJsonArray("conditions"), path)) {
                report.add("dropped " + path + " (conditions: needs another mod)");
                return null;
            }
            advancement.remove("conditions");
        }
        JsonObject display = advancement.getAsJsonObject("display");
        if (display != null && display.get("icon") instanceof JsonObject icon && icon.has("item")) {
            JsonObject upgraded = new JsonObject();
            upgraded.add("id", icon.get("item"));
            if (icon.has("count")) upgraded.add("count", icon.get("count"));
            display.add("icon", upgraded);
        }
        JsonObject criteria = advancement.getAsJsonObject("criteria");
        if (criteria != null) {
            for (String name : criteria.keySet()) {
                JsonObject criterion = criteria.getAsJsonObject(name);
                if (criterion.get("conditions") instanceof JsonObject conditions) {
                    upgradeCriterionConditions(criterion.has("trigger") ? criterion.get("trigger").getAsString() : "", conditions);
                }
            }
        }
        return advancement;
    }

    static void upgradeCriterionConditions(String trigger, JsonObject conditions) {
        if (trigger.equals("minecraft:recipe_unlocked") && conditions.get("recipe") instanceof JsonPrimitive recipe) {
            JsonArray recipes = new JsonArray();
            recipes.add(recipe);
            conditions.remove("recipe");
            conditions.add("recipes", recipes);
        }
        for (String key : List.copyOf(conditions.keySet())) {
            JsonElement value = conditions.get(key);
            if (ENTITY_CONTEXT_KEYS.contains(key) && value instanceof JsonObject entity && !entity.has("condition")) {
                conditions.add(key, LootTableFix.entityCondition(entity));
            } else if (key.equals("damage") && value instanceof JsonObject damage) {
                if (damage.get("type") instanceof JsonObject source) LootTableFix.damageSourcePredicate(source);
                if (damage.get("source_entity") instanceof JsonObject entity) damage.add("source_entity", LootTableFix.entityPredicate(entity));
            } else if (key.equals("killing_blow") && value instanceof JsonObject source) {
                LootTableFix.damageSourcePredicate(source);
            } else if (isConditionList(value)) {
                conditions.add(key, LootTableFix.conditions(value.getAsJsonArray()));
            } else if (key.equals("items") && value.isJsonArray()) {
                JsonArray out = new JsonArray();
                for (JsonElement item : value.getAsJsonArray()) out.add(item.isJsonObject() ? itemPredicate(item.getAsJsonObject()) : item);
                conditions.add(key, out);
            } else if (key.equals("item") && value.isJsonObject()) {
                conditions.add(key, itemPredicate(value.getAsJsonObject()));
            }
        }
    }

    /** 1.20.1 ContextAwarePredicate: a list of loot conditions (all must pass). */
    private static boolean isConditionList(JsonElement value) {
        if (!value.isJsonArray() || value.getAsJsonArray().isEmpty()) return false;
        for (JsonElement e : value.getAsJsonArray()) {
            if (!e.isJsonObject() || !e.getAsJsonObject().has("condition")) return false;
        }
        return true;
    }

    /** 1.20.1 item predicate: {@code tag} becomes {@code items: "#tag"}, {@code nbt} a custom_data component match. */
    static JsonObject itemPredicate(JsonObject predicate) {
        if (predicate.has("tag")) {
            predicate.addProperty("items", "#" + predicate.remove("tag").getAsString());
        }
        if (predicate.has("nbt")) {
            String nbt = predicate.remove("nbt").getAsString();
            JsonObject predicates = new JsonObject();
            Matcher damage = DAMAGE_NBT.matcher(nbt);
            if (damage.matches()) {
                // Damage moved out of the tag into the minecraft:damage component.
                JsonObject d = new JsonObject();
                d.addProperty("damage", Integer.parseInt(damage.group(1)));
                predicates.add("minecraft:damage", d);
            } else {
                predicates.addProperty("minecraft:custom_data", nbt);
            }
            predicate.add("predicates", predicates);
        }
        return predicate;
    }

    private boolean conditionsMet(JsonArray conditions, String path) {
        if (conditions == null) return true;
        for (JsonElement c : conditions) {
            if (!conditionMet(c.getAsJsonObject())) return false;
        }
        return true;
    }

    private boolean conditionMet(JsonObject c) {
        String type = c.has("type") ? c.get("type").getAsString() : "";
        return switch (type) {
            case "forge:mod_loaded" -> knownMods.contains(c.get("modid").getAsString());
            case "forge:not" -> !conditionMet(c.getAsJsonObject("value"));
            case "forge:and" -> {
                for (JsonElement v : c.getAsJsonArray("values")) if (!conditionMet(v.getAsJsonObject())) yield false;
                yield true;
            }
            case "forge:or" -> {
                for (JsonElement v : c.getAsJsonArray("values")) if (conditionMet(v.getAsJsonObject())) yield true;
                yield false;
            }
            case "forge:true" -> true;
            case "forge:false" -> false;
            // Item/tag existence and mod-specific (config) conditions: assume their default (present / enabled).
            default -> true;
        };
    }
}

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
 *   <li>Forge global loot modifiers: their loot conditions, like loot tables';</li>
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
    /**
     * Vanilla tags 26.x renamed, per tag folder: 1.20.1 name to 26.3 name. Each pair is the tag the same vanilla
     * check reads in both versions (e.g. BambooStalkBlock.canSurvive: BAMBOO_PLANTABLE_ON, then SUPPORTS_BAMBOO), so a
     * mod adding its blocks to the old tag means the new one.
     */
    static final Map<String, Map<String, String>> VANILLA_TAG_RENAMES = Map.of(
            "tags/block", Map.of(
                    "bamboo_plantable_on", "supports_bamboo",
                    "big_dripleaf_placeable", "supports_big_dripleaf",
                    "small_dripleaf_placeable", "supports_small_dripleaf",
                    "dead_bush_may_place_on", "supports_dry_vegetation",
                    "mushroom_grow_block", "overrides_mushroom_light_requirement",
                    "snow_layer_can_survive_on", "support_override_snow_layer",
                    "snow_layer_cannot_survive_on", "cannot_support_snow_layer",
                    "convertable_to_mud", "convertible_to_mud"),
            "tags/item", Map.of("axolotl_tempt_items", "axolotl_food"));
    /** Criterion fields that were 1.20.1 ContextAwarePredicates (a condition list, or a bare entity predicate). */
    private static final Set<String> ENTITY_CONTEXT_KEYS = Set.of("player", "entity", "child", "parent", "partner", "zombie",
            "villager", "projectile", "shooter", "lightning", "bystander", "source");
    private static final Pattern DAMAGE_NBT = Pattern.compile("\\{\\s*Damage\\s*:\\s*(\\d+)\\s*}");
    private static final Pattern ITEM_MODEL = Pattern.compile("assets/([^/]+)/models/item/(.+)\\.json");
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

    /** Generated item tags for ingredient lists with tags ({@code ns:path} to values); see {@link #ingredient}. */
    private final Map<String, JsonArray> anyOfTags = new java.util.TreeMap<>();
    private String recipeNamespace = "rose";

    /** Item models seen ({@code ns:path}); 1.21.4 needs an item definition for each. */
    private final Set<String> itemModels = new java.util.TreeSet<>();
    /** Item models' 1.20.1 {@code overrides} (pick a model by item property), by model. */
    private final Map<String, JsonArray> modelOverrides = new java.util.HashMap<>();

    /**
     * Files the mod lacks for 26.3: an item definition ({@code assets/<ns>/items/<id>.json}) for every item model.
     * 1.21.4 made item models selected by these definitions; without one an item renders as missing. Also the item
     * tags generated for recipe ingredient lists ({@link #ingredient}). Call after every file went through fix().
     */
    public List<Fixed> extras() {
        List<Fixed> out = new ArrayList<>();
        for (String item : itemModels) {
            int colon = item.indexOf(':');
            JsonObject model = plainModel(item.substring(0, colon) + ":item/" + item.substring(colon + 1));
            JsonObject definition = new JsonObject();
            definition.add("model", modelOverrides.containsKey(item) ? withOverrides(item, model, modelOverrides.get(item)) : model);
            out.add(new Fixed("assets/" + item.substring(0, colon) + "/items/" + item.substring(colon + 1) + ".json",
                    GSON.toJson(definition).getBytes(StandardCharsets.UTF_8)));
        }
        anyOfTags.forEach((id, values) -> {
            int colon = id.indexOf(':');
            JsonObject tag = new JsonObject();
            tag.add("values", values);
            out.add(new Fixed("data/" + id.substring(0, colon) + "/tags/item/" + id.substring(colon + 1) + ".json",
                    GSON.toJson(tag).getBytes(StandardCharsets.UTF_8)));
        });
        return out;
    }

    private static JsonObject plainModel(String model) {
        JsonObject out = new JsonObject();
        out.addProperty("type", "minecraft:model");
        out.addProperty("model", model);
        return out;
    }

    /**
     * 1.20.1 {@code overrides} over one item property become a {@code range_dispatch} on {@code rose:legacy_property},
     * which asks the property the mod registered (era ItemProperties): the highest threshold reached picks the model,
     * as the last matching override did for overrides in rising order. Overrides over several properties keep the
     * plain model (reported).
     */
    private JsonObject withOverrides(String item, JsonObject fallback, JsonArray overrides) {
        String property = null;
        JsonArray entries = new JsonArray();
        for (JsonElement o : overrides) {
            JsonObject override = o.getAsJsonObject();
            JsonObject predicate = override.getAsJsonObject("predicate");
            if (predicate == null || predicate.size() != 1 || !override.has("model")) property = "";
            else {
                String key = predicate.keySet().iterator().next();
                String id = key.contains(":") ? key : "minecraft:" + key;
                if (property == null) property = id;
                else if (!property.equals(id)) property = "";
                JsonObject entry = new JsonObject();
                entry.addProperty("threshold", predicate.get(key).getAsFloat());
                entry.add("model", plainModel(override.get("model").getAsString()));
                entries.add(entry);
            }
            if ("".equals(property)) {
                report.add("kept the plain model for " + item + ": its overrides use several properties");
                return fallback;
            }
        }
        JsonObject dispatch = new JsonObject();
        dispatch.addProperty("type", "minecraft:range_dispatch");
        dispatch.addProperty("property", "rose:legacy_property");
        dispatch.addProperty("name", property);
        dispatch.add("entries", entries);
        dispatch.add("fallback", fallback);
        return dispatch;
    }

    /** The fixed resource, or {@code null} to leave it out of the translated jar. */
    public Fixed fix(String path, byte[] content) {
        Matcher item = ITEM_MODEL.matcher(path);
        if (item.matches()) {
            itemModels.add(item.group(1) + ":" + item.group(2));
            JsonObject model = parse(content);
            if (model != null && model.get("overrides") instanceof JsonArray overrides && !overrides.isEmpty()) {
                modelOverrides.put(item.group(1) + ":" + item.group(2), overrides);
            }
        }
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
            Map<String, String> renames = tagRenames(folder);
            if (namespace.equals("minecraft")) {
                String kind = folder.substring(0, folder.indexOf('/', "tags/".length()));
                String name = folder.substring(kind.length() + 1, folder.length() - ".json".length());
                if (renames.containsKey(name)) {
                    newPath = "data/minecraft/" + kind + "/" + renames.get(name) + ".json";
                    report.add("moved " + path + " to " + newPath + " (26.x renamed #minecraft:" + name + ")");
                }
            }
            JsonObject tag = parse(content);
            if (tag == null) return new Fixed(newPath, null);
            return new Fixed(newPath, GSON.toJson(renameTagReferences(optionalEntries(tag), renames)).getBytes(StandardCharsets.UTF_8));
        }
        if (folder.startsWith("recipe/") && path.endsWith(".json")) {
            JsonObject recipe = parse(content);
            if (recipe == null) return new Fixed(newPath, null);
            recipeNamespace = namespace;
            JsonObject upgraded = upgradeRecipe(recipe, path);
            if (upgraded == null) return null;
            return new Fixed(newPath, GSON.toJson(upgraded).getBytes(StandardCharsets.UTF_8));
        }
        if (folder.startsWith("loot_modifiers/") && path.endsWith(".json") && !folder.equals("loot_modifiers/global_loot_modifiers.json")) {
            JsonObject modifier = parse(content);
            if (modifier == null || !(modifier.get("conditions") instanceof JsonArray conditions)) return new Fixed(newPath, null);
            // Forge global loot modifiers: their conditions are 1.20.1 loot conditions (LootTableFix), kept as a list
            JsonArray upgraded = new JsonArray();
            conditions.forEach(c -> upgraded.add(c.isJsonObject() ? LootTableFix.condition(c.getAsJsonObject()) : c));
            modifier.add("conditions", upgraded);
            return new Fixed(newPath, GSON.toJson(modifier).getBytes(StandardCharsets.UTF_8));
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
    /** The vanilla tag renames for a tag file's folder ({@code tags/block/...}), or none. */
    private static Map<String, String> tagRenames(String folder) {
        for (var e : VANILLA_TAG_RENAMES.entrySet()) {
            if (folder.startsWith(e.getKey() + "/")) return e.getValue();
        }
        return Map.of();
    }

    /** Entries naming a renamed vanilla tag ({@code #minecraft:old}) name the 26.3 tag. */
    static JsonObject renameTagReferences(JsonObject tag, Map<String, String> renames) {
        JsonArray values = tag.getAsJsonArray("values");
        if (values == null || renames.isEmpty()) return tag;
        for (JsonElement v : values) {
            JsonObject entry = v.getAsJsonObject();
            String id = entry.get("id").getAsString();
            if (id.startsWith("#minecraft:") && renames.containsKey(id.substring("#minecraft:".length()))) {
                entry.addProperty("id", "#minecraft:" + renames.get(id.substring("#minecraft:".length())));
            }
        }
        return tag;
    }

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
     * a list of items becomes a list of ids. A 1.21.2 list holds item ids only, so a list with tags (any cooked meat:
     * {@code [{"tag": "forge:cooked_beef"}, {"tag": "forge:cooked_pork"}, ...]}) becomes a generated item tag with
     * all of its entries ({@link #anyOfTag}). Lists with custom ingredient types can't be expressed; their first tag
     * is kept and reported.
     */
    JsonElement ingredient(JsonElement old) {
        if (old == null) return null;
        if (old.isJsonArray()) {
            List<JsonElement> parts = new ArrayList<>();
            for (JsonElement e : old.getAsJsonArray()) parts.add(ingredient(e));
            if (parts.size() == 1) return parts.getFirst();
            boolean allIds = parts.stream().allMatch(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isString());
            boolean anyTag = parts.stream().anyMatch(DataPackFix::isTagReference);
            if (allIds && anyTag) return new com.google.gson.JsonPrimitive("#" + anyOfTag(parts));
            if (anyTag) {
                JsonElement first = parts.stream().filter(DataPackFix::isTagReference).findFirst().orElseThrow();
                report.add("ingredient list with custom types reduced to " + first + ": " + old);
                return first;
            }
            JsonArray items = new JsonArray();
            parts.forEach(items::add);
            return items;
        }
        if (!old.isJsonObject()) return old;
        JsonObject o = old.getAsJsonObject();
        if (o.has("item")) return o.get("item");
        if (o.has("tag")) return new com.google.gson.JsonPrimitive("#" + o.get("tag").getAsString());
        return old;
    }

    private static boolean isTagReference(JsonElement e) {
        return e.isJsonPrimitive() && e.getAsString().startsWith("#");
    }

    /**
     * The id of a generated item tag holding {@code entries} (item ids and {@code #tags}), in the namespace of the
     * recipe being upgraded. Named by its content, so recipes that list the same alternatives share one tag.
     */
    private String anyOfTag(List<JsonElement> entries) {
        JsonArray values = new JsonArray();
        StringBuilder key = new StringBuilder();
        for (JsonElement e : entries) {
            JsonObject entry = new JsonObject();
            entry.add("id", e);
            entry.addProperty("required", false);
            values.add(entry);
            key.append(e.getAsString()).append('\n');
        }
        String id = recipeNamespace + ":rose/any_of/" + java.util.HexFormat.of().formatHex(sha1(key.toString())).substring(0, 12);
        anyOfTags.put(id, values);
        return id;
    }

    private static byte[] sha1(String s) {
        try {
            return java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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

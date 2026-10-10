package rose.packfix.v1_20_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Upgrades a 1.20.1 loot table (or a condition, for advancements) to 26.3's format. Checked against vanilla's own
 * tables in both versions (LootTableFixTest's oracle); the changes are:
 * <ul>
 *   <li>{@code conditions: [..]} became one {@code condition} (several: {@code all_of}); {@code functions: [..]}
 *       became {@code modifier} (one function, or an inline list);</li>
 *   <li>conditions and functions are dispatched on {@code type} instead of {@code condition} / {@code function};</li>
 *   <li>renamed types: {@code block_state_property} to {@code match_block} ({@code blocks}, {@code state}),
 *       {@code looting_enchant} to {@code enchanted_count_increase}, {@code random_chance_with_looting} to
 *       {@code random_chance_with_enchanted_bonus};</li>
 *   <li>enchanting functions take {@code options} (1.21 data-driven enchantments);</li>
 *   <li>item predicates name tags in {@code items} and enchantments under {@code predicates}; entity predicates are
 *       maps of namespaced sub-predicates ({@code minecraft:flags}, {@code minecraft:entity_type}, ...).</li>
 * </ul>
 */
final class LootTableFix {
    private static final Set<String> ENTITY_NESTED = Set.of("vehicle", "passenger", "targeted_entity");
    /** Function fields holding number providers (1.20.1 allowed an untyped {min, max} for uniform). */
    private static final Set<String> NUMBER_FIELDS = Set.of("count", "damage", "levels", "rolls", "bonus_rolls");

    static JsonObject table(JsonObject table) {
        moveFunctions(table);
        if (table.get("pools") instanceof JsonArray pools) {
            for (JsonElement pool : pools) pool(pool.getAsJsonObject());
        }
        return table;
    }

    private static void pool(JsonObject pool) {
        moveConditions(pool);
        moveFunctions(pool);
        if (pool.get("bonus_rolls") instanceof JsonPrimitive bonus && bonus.isNumber() && bonus.getAsDouble() == 0) pool.remove("bonus_rolls");
        numbers(pool);
        if (pool.get("entries") instanceof JsonArray entries) {
            for (JsonElement entry : entries) entry(entry.getAsJsonObject());
        }
    }

    private static void entry(JsonObject entry) {
        moveConditions(entry);
        moveFunctions(entry);
        if (entry.get("children") instanceof JsonArray children) {
            for (JsonElement child : children) entry(child.getAsJsonObject());
        }
    }

    private static void moveConditions(JsonObject o) {
        if (o.get("conditions") instanceof JsonArray conditions) {
            o.remove("conditions");
            o.add("condition", conditions(conditions));
        }
    }

    private static void moveFunctions(JsonObject o) {
        if (o.get("functions") instanceof JsonArray functions) {
            o.remove("functions");
            JsonArray out = new JsonArray();
            for (JsonElement f : functions) out.add(function(f.getAsJsonObject()));
            o.add("modifier", out.size() == 1 ? out.get(0) : out);
        }
    }

    /** A 1.20.1 condition list (all must pass) as one 26.3 condition. */
    static JsonElement conditions(JsonArray conditions) {
        JsonArray out = new JsonArray();
        for (JsonElement c : conditions) out.add(condition(c.getAsJsonObject()));
        if (out.size() == 1) return out.get(0);
        JsonObject all = new JsonObject();
        all.addProperty("type", "minecraft:all_of");
        all.add("terms", out);
        return all;
    }

    static JsonObject condition(JsonObject c) {
        if (!c.has("condition")) return c; // already 26.3-shaped
        String type = namespaced(c.remove("condition").getAsString());
        entityTarget(c, "entity");
        switch (type) {
            case "minecraft:block_state_property" -> {
                type = "minecraft:match_block";
                rename(c, "block", "blocks");
                rename(c, "properties", "state");
            }
            case "minecraft:random_chance_with_looting" -> {
                type = "minecraft:random_chance_with_enchanted_bonus";
                // 1.20.1: chance + looting level * multiplier
                double chance = c.remove("chance").getAsDouble();
                double perLevel = c.has("looting_multiplier") ? c.remove("looting_multiplier").getAsDouble() : 0;
                JsonObject linear = new JsonObject();
                linear.addProperty("type", "minecraft:linear");
                linear.addProperty("base", (float) (chance + perLevel));
                linear.addProperty("per_level_above_first", (float) perLevel);
                c.addProperty("enchantment", "minecraft:looting");
                c.addProperty("unenchanted_chance", (float) chance);
                c.add("enchanted_chance", linear);
            }
            case "minecraft:match_tool" -> {
                if (c.get("predicate") instanceof JsonObject p) c.add("predicate", itemPredicate(p));
            }
            case "minecraft:entity_properties" -> {
                if (c.get("predicate") instanceof JsonObject p) c.add("predicate", entityPredicate(p));
            }
            case "minecraft:damage_source_properties" -> {
                if (c.get("predicate") instanceof JsonObject p) damageSourcePredicate(p);
            }
            default -> { }
        }
        if (c.get("terms") instanceof JsonArray terms) {
            JsonArray out = new JsonArray();
            for (JsonElement t : terms) out.add(t.isJsonObject() ? condition(t.getAsJsonObject()) : t);
            c.add("terms", out);
        }
        if (c.get("term") instanceof JsonObject term) c.add("term", condition(term));
        c.addProperty("type", type);
        return c;
    }

    static JsonObject function(JsonObject f) {
        if (!f.has("function")) return f;
        String type = namespaced(f.remove("function").getAsString());
        entityTarget(f, "entity");
        entityTarget(f, "source"); // copy_name, copy_nbt
        switch (type) {
            case "minecraft:set_count", "minecraft:set_damage" -> {
                if (f.get("add") instanceof JsonPrimitive add && !add.getAsBoolean()) f.remove("add");
            }
            case "minecraft:looting_enchant" -> {
                type = "minecraft:enchanted_count_increase";
                f.addProperty("enchantment", "minecraft:looting");
            }
            case "minecraft:enchant_randomly" -> {
                if (f.has("enchantments")) f.add("options", f.remove("enchantments"));
                else f.addProperty("options", "#minecraft:on_random_loot");
            }
            case "minecraft:enchant_with_levels" -> {
                boolean treasure = f.has("treasure") && f.remove("treasure").getAsBoolean();
                f.addProperty("options", treasure ? "#minecraft:on_random_loot" : "#minecraft:in_enchanting_table");
            }
            default -> { }
        }
        moveConditions(f);
        numbers(f);
        f.addProperty("type", type);
        return f;
    }

    /** 1.20.1 item predicate: {@code tag} into {@code items}, {@code enchantments} under {@code predicates}. */
    static JsonObject itemPredicate(JsonObject p) {
        JsonObject predicates = p.get("predicates") instanceof JsonObject existing ? existing : new JsonObject();
        if (p.has("tag")) p.addProperty("items", "#" + p.remove("tag").getAsString());
        if (p.get("enchantments") instanceof JsonArray enchantments) {
            p.remove("enchantments");
            for (JsonElement e : enchantments) {
                if (e instanceof JsonObject o) rename(o, "enchantment", "enchantments");
            }
            predicates.add("minecraft:enchantments", enchantments);
        }
        if (p.get("stored_enchantments") instanceof JsonArray stored) {
            p.remove("stored_enchantments");
            for (JsonElement e : stored) {
                if (e instanceof JsonObject o) rename(o, "enchantment", "enchantments");
            }
            predicates.add("minecraft:stored_enchantments", stored);
        }
        if (!predicates.isEmpty()) p.add("predicates", predicates);
        return p;
    }

    /** Damage source predicate: entity predicates inside, and {@code tags} ids name tags ({@code #id}) in 26.3. */
    static JsonObject damageSourcePredicate(JsonObject p) {
        for (String k : List.of("direct_entity", "source_entity")) {
            if (p.get(k) instanceof JsonObject e) p.add(k, entityPredicate(e));
        }
        if (p.get("tags") instanceof JsonArray tags) {
            for (JsonElement t : tags) {
                if (t instanceof JsonObject tag && tag.get("id") instanceof JsonPrimitive id && !id.getAsString().startsWith("#")) {
                    tag.addProperty("id", "#" + id.getAsString());
                }
            }
        }
        return p;
    }

    /** 1.20.1 allowed a bare entity predicate where a condition list was expected: it tested "this" entity. */
    static JsonObject entityCondition(JsonObject entityPredicate) {
        JsonObject c = new JsonObject();
        c.addProperty("type", "minecraft:entity_properties");
        c.addProperty("entity", "this");
        c.add("predicate", entityPredicate(entityPredicate));
        return c;
    }

    /** 1.20.1 entity predicate: fixed keys became namespaced sub-predicate types. */
    static JsonObject entityPredicate(JsonObject p) {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, JsonElement> e : p.entrySet()) {
            String key = e.getKey();
            JsonElement value = e.getValue();
            if (key.equals("type")) {
                out.add("minecraft:entity_type", value);
            } else if (key.equals("type_specific") && value instanceof JsonObject specific) {
                String sub = specific.remove("type").getAsString().replace("minecraft:", "");
                out.add("minecraft:type_specific/" + sub, specific);
            } else if (ENTITY_NESTED.contains(key) && value instanceof JsonObject nested) {
                out.add("minecraft:" + key, entityPredicate(nested));
            } else if (key.equals("equipment") && value instanceof JsonObject slots) {
                JsonObject equipment = new JsonObject();
                for (var slot : slots.entrySet()) {
                    equipment.add(slot.getKey(), slot.getValue() instanceof JsonObject item ? itemPredicate(item) : slot.getValue());
                }
                out.add("minecraft:equipment", equipment);
            } else {
                out.add(key.contains(":") ? key : "minecraft:" + key, value);
            }
        }
        return out;
    }

    private static void numbers(JsonObject o) {
        for (String field : NUMBER_FIELDS) {
            if (o.get(field) instanceof JsonObject n && !n.has("type") && n.has("min") && n.has("max")) {
                n.addProperty("type", "minecraft:uniform");
            }
        }
    }

    /** 1.21 renamed the loot entity targets: killer to attacker, direct_killer, killer_player (LootContext.EntityTarget). */
    private static void entityTarget(JsonObject o, String key) {
        if (!(o.get(key) instanceof JsonPrimitive p) || !p.isString()) return;
        String renamed = switch (p.getAsString()) {
            case "killer" -> "attacker";
            case "direct_killer" -> "direct_attacker";
            case "killer_player" -> "attacking_player";
            default -> null;
        };
        if (renamed != null) o.addProperty(key, renamed);
    }

    private static void rename(JsonObject o, String from, String to) {
        if (o.has(from)) o.add(to, o.remove(from));
    }

    private static String namespaced(String id) {
        return id.contains(":") ? id : "minecraft:" + id;
    }

    private LootTableFix() {}
}

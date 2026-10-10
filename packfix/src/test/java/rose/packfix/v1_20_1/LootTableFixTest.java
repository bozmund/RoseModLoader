package rose.packfix.v1_20_1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

class LootTableFixTest {
    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void killerEntityTargetsBecomeAttackers() {
        JsonObject c = LootTableFix.condition(json("""
                {"condition":"minecraft:entity_properties","entity":"killer","predicate":{}}"""));
        assertEquals("attacker", c.get("entity").getAsString());
        assertEquals("attacking_player", LootTableFix.condition(json("""
                {"condition":"minecraft:entity_properties","entity":"killer_player","predicate":{}}""")).get("entity").getAsString());
        assertEquals("direct_attacker", LootTableFix.function(json("""
                {"function":"minecraft:copy_name","source":"direct_killer"}""")).get("source").getAsString());
        assertEquals("this", LootTableFix.condition(json("""
                {"condition":"minecraft:entity_properties","entity":"this","predicate":{}}""")).get("entity").getAsString());
    }

    @Test
    void wheatUpgradesExactlyToVanilla26() {
        // evidence: data/minecraft/loot_tables/blocks/wheat.json (1.20.1) vs loot_table/blocks/wheat.json (26.3)
        JsonObject old = json("""
                {"type":"minecraft:block","functions":[{"function":"minecraft:explosion_decay"}],"pools":[
                 {"bonus_rolls":0.0,"rolls":1.0,"entries":[{"type":"minecraft:alternatives","children":[
                   {"type":"minecraft:item","name":"minecraft:wheat","conditions":[{"condition":"minecraft:block_state_property","block":"minecraft:wheat","properties":{"age":"7"}}]},
                   {"type":"minecraft:item","name":"minecraft:wheat_seeds"}]}]},
                 {"bonus_rolls":0.0,"rolls":1.0,"conditions":[{"condition":"minecraft:block_state_property","block":"minecraft:wheat","properties":{"age":"7"}}],
                  "entries":[{"type":"minecraft:item","name":"minecraft:wheat_seeds","functions":[{"function":"minecraft:apply_bonus","enchantment":"minecraft:fortune",
                   "formula":"minecraft:binomial_with_bonus_count","parameters":{"extra":3,"probability":0.5714286}}]}]}],
                 "random_sequence":"minecraft:blocks/wheat"}""");
        JsonObject want = json("""
                {"type":"minecraft:block","modifier":{"type":"minecraft:explosion_decay"},"pools":[
                 {"rolls":1,"entries":[{"type":"minecraft:alternatives","children":[
                   {"type":"minecraft:item","name":"minecraft:wheat","condition":{"type":"minecraft:match_block","blocks":"minecraft:wheat","state":{"age":"7"}}},
                   {"type":"minecraft:item","name":"minecraft:wheat_seeds"}]}]},
                 {"rolls":1,"condition":{"type":"minecraft:match_block","blocks":"minecraft:wheat","state":{"age":"7"}},
                  "entries":[{"type":"minecraft:item","name":"minecraft:wheat_seeds","modifier":{"type":"minecraft:apply_bonus","enchantment":"minecraft:fortune",
                   "formula":"minecraft:binomial_with_bonus_count","parameters":{"extra":3,"probability":0.5714286}}}]}],
                 "random_sequence":"minecraft:blocks/wheat"}""");
        assertEquals(canon(want, null), canon(LootTableFix.table(old), null));
    }

    @Test
    void lootingChanceBecomesEnchantedBonus() {
        // evidence: entities/wither_skeleton.json, 1.20.1 vs 26.3
        JsonObject c = LootTableFix.condition(json("{\"condition\":\"minecraft:random_chance_with_looting\",\"chance\":0.025,\"looting_multiplier\":0.01}"));
        assertEquals("minecraft:random_chance_with_enchanted_bonus", c.get("type").getAsString());
        assertEquals(0.025f, c.get("unenchanted_chance").getAsFloat());
        assertEquals(0.035f, c.getAsJsonObject("enchanted_chance").get("base").getAsFloat(), 1e-6);
        assertEquals(0.01f, c.getAsJsonObject("enchanted_chance").get("per_level_above_first").getAsFloat(), 1e-6);
    }

    @Test
    void functionsAndPredicatesFollowTheirRenames() {
        JsonObject looting = LootTableFix.function(json("{\"function\":\"minecraft:looting_enchant\",\"count\":{\"min\":0,\"max\":1}}"));
        assertEquals("minecraft:enchanted_count_increase", looting.get("type").getAsString());
        assertEquals("minecraft:looting", looting.get("enchantment").getAsString());
        assertEquals("minecraft:uniform", looting.getAsJsonObject("count").get("type").getAsString(), "untyped {min, max} was uniform");

        JsonObject randomly = LootTableFix.function(json("{\"function\":\"minecraft:enchant_randomly\"}"));
        assertEquals("#minecraft:on_random_loot", randomly.get("options").getAsString());

        JsonObject entity = LootTableFix.entityPredicate(json("{\"type\":\"#minecraft:raiders\",\"flags\":{\"is_on_fire\":true},"
                + "\"type_specific\":{\"type\":\"player\",\"gamemode\":\"survival\"}}"));
        assertEquals(Set.of("minecraft:entity_type", "minecraft:flags", "minecraft:type_specific/player"), entity.keySet());

        JsonObject tool = LootTableFix.itemPredicate(json("{\"tag\":\"forge:shears\",\"enchantments\":[{\"enchantment\":\"minecraft:silk_touch\"}]}"));
        assertEquals("#forge:shears", tool.get("items").getAsString());
        assertEquals("minecraft:silk_touch", tool.getAsJsonObject("predicates").getAsJsonArray("minecraft:enchantments")
                .get(0).getAsJsonObject().get("enchantments").getAsString());
    }

    /**
     * Oracle: every vanilla table present in both versions, upgraded from 1.20.1, against 26.3's. Tables whose content
     * Mojang changed (new drops, component copies instead of NBT copies) can't match; the floor guards regressions.
     */
    @Test
    void vanillaTablesMatchTheirTwentySixThreeVersions() throws IOException {
        Path corpus = Path.of("..", "corpus", "minecraft");
        assumeTrue(Files.exists(corpus.resolve("1.20.1/client.jar")) && Files.exists(corpus.resolve("26.3/client.jar")),
                "needs the corpus (./gradlew corpusSetup)");
        Map<String, JsonObject> predicates = new TreeMap<>();
        Map<String, JsonObject> newer = new TreeMap<>();
        try (ZipFile z = new ZipFile(corpus.resolve("26.3/client.jar").toFile())) {
            for (var e : java.util.Collections.list(z.entries())) {
                String n = e.getName();
                if (!n.endsWith(".json")) continue;
                if (n.startsWith("data/minecraft/loot_table/")) newer.put(n.substring(26), read(z, e));
                if (n.startsWith("data/minecraft/predicate/")) predicates.put("minecraft:" + n.substring(25, n.length() - 5), read(z, e));
            }
        }
        int matched = 0;
        List<String> mismatched = new ArrayList<>();
        try (ZipFile z = new ZipFile(corpus.resolve("1.20.1/client.jar").toFile())) {
            for (var e : java.util.Collections.list(z.entries())) {
                String n = e.getName();
                if (!n.startsWith("data/minecraft/loot_tables/") || !n.endsWith(".json")) continue;
                JsonObject want = newer.get(n.substring(27));
                if (want == null) continue;
                String got = canon(LootTableFix.table(read(z, e)), predicates);
                if (got.equals(canon(want, predicates))) matched++;
                else mismatched.add(n.substring(27));
            }
        }
        System.out.println("loot oracle: " + matched + " match, " + mismatched.size() + " differ: " + mismatched);
        assertTrue(matched >= 976, "loot oracle regressed: " + matched + " match");
    }

    private static JsonObject read(ZipFile z, java.util.zip.ZipEntry e) throws IOException {
        try (var in = z.getInputStream(e)) {
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static final Set<String> HOLDER_LISTS = Set.of("items", "blocks", "enchantments", "options");

    /**
     * Comparison form: sorted keys, integral numbers as ints, one-element holder lists as their element, predicate
     * references inlined, and the equivalent spellings 26.3 vanilla chose ({@code copy_components} of the custom name
     * for {@code copy_name}) folded together.
     */
    private static String canon(JsonElement e, Map<String, JsonObject> predicates) {
        return normalize(e, null, predicates).toString();
    }

    private static JsonElement normalize(JsonElement e, String key, Map<String, JsonObject> predicates) {
        if (e instanceof JsonPrimitive p) {
            if (p.isNumber() && p.getAsDouble() == Math.rint(p.getAsDouble())) return new JsonPrimitive(p.getAsLong());
            if (p.isString() && predicates != null && Set.of("terms", "term", "condition").contains(key) && predicates.containsKey(p.getAsString())) {
                return normalize(predicates.get(p.getAsString()), null, predicates);
            }
            return p;
        }
        if (e instanceof JsonArray a) {
            if (HOLDER_LISTS.contains(key) && a.size() == 1 && a.get(0).isJsonPrimitive()) return a.get(0);
            JsonArray out = new JsonArray();
            for (JsonElement x : a) out.add(normalize(x, key, predicates));
            return out;
        }
        if (e instanceof JsonObject o) {
            JsonObject sorted = new JsonObject();
            new TreeMap<>(o.asMap()).forEach((k, v) -> sorted.add(k, normalize(v, k, predicates)));
            if (sorted.has("type") && sorted.get("type").getAsString().equals("minecraft:copy_components")
                    && "[\"minecraft:custom_name\"]".equals(String.valueOf(sorted.get("include"))) && sorted.size() == 3) {
                JsonObject copyName = new JsonObject();
                copyName.add("source", sorted.get("source"));
                copyName.addProperty("type", "minecraft:copy_name");
                return copyName;
            }
            return sorted;
        }
        return e;
    }
}

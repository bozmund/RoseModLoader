package rose.packfix.v1_20_1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DataPackFixTest {
    private final DataPackFix fix = new DataPackFix(Set.of("minecraft", "forge", "farmersdelight"));

    private JsonObject upgrade(String path, String json) {
        DataPackFix.Fixed fixed = fix.fix(path, json.getBytes(StandardCharsets.UTF_8));
        return fixed == null ? null : JsonParser.parseString(new String(fixed.content(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    void recipeAdvancementGetsRecipesListAndSingularFolder() {
        String json = """
                {"parent":"minecraft:recipes/root",
                 "criteria":{"has_the_recipe":{"trigger":"minecraft:recipe_unlocked","conditions":{"recipe":"fd:crate"}},
                             "has_tag":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"tag":"forge:crops"}]}}},
                 "requirements":[["has_the_recipe","has_tag"]],"rewards":{"recipes":["fd:crate"]}}""";
        DataPackFix.Fixed fixed = fix.fix("data/fd/advancements/recipes/crate.json", json.getBytes(StandardCharsets.UTF_8));
        assertEquals("data/fd/advancement/recipes/crate.json", fixed.path());
        JsonObject criteria = upgrade("data/fd/advancements/recipes/crate.json", json).getAsJsonObject("criteria");
        assertEquals("[\"fd:crate\"]", criteria.getAsJsonObject("has_the_recipe").getAsJsonObject("conditions").get("recipes").toString());
        assertEquals("{\"items\":\"#forge:crops\"}",
                criteria.getAsJsonObject("has_tag").getAsJsonObject("conditions").getAsJsonArray("items").get(0).toString());
    }

    @Test
    void conditionListsBecomeOneConditionAndIconsUseId() {
        String json = """
                {"display":{"icon":{"item":"fd:skillet","nbt":"{Damage:0}"},"title":"t","description":"d"},
                 "criteria":{
                   "one":{"trigger":"minecraft:placed_block","conditions":{"location":[{"condition":"minecraft:block_state_property","block":"fd:rice"}]}},
                   "two":{"trigger":"minecraft:item_used_on_block","conditions":{"location":[
                       {"condition":"minecraft:location_check","predicate":{}},{"condition":"minecraft:match_tool","predicate":{}}]}},
                   "dmg":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"items":["fd:skillet"],"nbt":"{Damage:0}"}]}}}}""";
        JsonObject adv = upgrade("data/fd/advancements/main/a.json", json);
        assertEquals("{\"id\":\"fd:skillet\"}", adv.getAsJsonObject("display").get("icon").toString());
        JsonObject criteria = adv.getAsJsonObject("criteria");
        assertEquals("minecraft:match_block",
                criteria.getAsJsonObject("one").getAsJsonObject("conditions").getAsJsonObject("location").get("type").getAsString());
        JsonObject two = criteria.getAsJsonObject("two").getAsJsonObject("conditions").getAsJsonObject("location");
        assertEquals("minecraft:all_of", two.get("type").getAsString());
        assertEquals(2, two.getAsJsonArray("terms").size());
        assertEquals("{\"minecraft:damage\":{\"damage\":0}}",
                criteria.getAsJsonObject("dmg").getAsJsonObject("conditions").getAsJsonArray("items").get(0).getAsJsonObject().get("predicates").toString());
    }

    @Test
    void forgeConditionalAdvancementsPickTheFirstBranchThatApplies() {
        String json = """
                {"advancements":[
                  {"conditions":[{"type":"forge:mod_loaded","modid":"othermod"}],"advancement":{"criteria":{"a":{"trigger":"minecraft:tick"}}}},
                  {"conditions":[{"type":"forge:mod_loaded","modid":"farmersdelight"}],"advancement":{"criteria":{"b":{"trigger":"minecraft:tick"}}}}]}""";
        assertEquals(Set.of("b"), upgrade("data/fd/advancements/x.json", json).getAsJsonObject("criteria").keySet());
        assertNull(upgrade("data/fd/advancements/y.json", """
                {"advancements":[{"conditions":[{"type":"forge:mod_loaded","modid":"othermod"}],"advancement":{}}]}"""));
    }

    @Test
    void damageAndBareEntityPredicatesUpgrade() {
        // evidence: FD main/hit_raider_with_rotten_tomato (1.20.1) against 26.3 PlayerHurtEntityTrigger, TagPredicate
        String json = """
                {"criteria":{"hit":{"trigger":"minecraft:player_hurt_entity","conditions":{
                  "damage":{"type":{"direct_entity":{"type":"fd:rotten_tomato"},"tags":[{"expected":true,"id":"minecraft:is_projectile"}]}},
                  "entity":{"type":"#minecraft:raiders"}}}}}""";
        JsonObject conditions = upgrade("data/fd/advancements/main/hit.json", json).getAsJsonObject("criteria")
                .getAsJsonObject("hit").getAsJsonObject("conditions");
        JsonObject source = conditions.getAsJsonObject("damage").getAsJsonObject("type");
        assertEquals("fd:rotten_tomato", source.getAsJsonObject("direct_entity").get("minecraft:entity_type").getAsString());
        assertEquals("#minecraft:is_projectile", source.getAsJsonArray("tags").get(0).getAsJsonObject().get("id").getAsString());
        JsonObject entity = conditions.getAsJsonObject("entity");
        assertEquals("minecraft:entity_properties", entity.get("type").getAsString());
        assertEquals("#minecraft:raiders", entity.getAsJsonObject("predicate").get("minecraft:entity_type").getAsString());
    }

    @Test
    void vanillaIdsRenamedSince1201AreUpdated() {
        // evidence: 26.3 DataFixers v4541 "Rename chain to iron_chain"
        String json = """
                {"type":"minecraft:crafting_shaped","pattern":["X"],"key":{"X":{"item":"minecraft:chain"}},"result":{"item":"fd:sign"}}""";
        JsonObject recipe = upgrade("data/fd/recipes/sign.json", json);
        assertEquals("minecraft:iron_chain", recipe.getAsJsonObject("key").get("X").getAsString());
    }

    @Test
    void configuredFeaturesMoveToFeatureAndFlatten() {
        // evidence: vanilla 1.20.1 configured_feature/patch_berry_bush (inner simple_block) vs 26.3 feature/berry_bush
        String json = """
                {"type":"fd:wild_crop","config":{"tries":64,"primary_feature":{"feature":{"type":"minecraft:simple_block","config":{
                  "to_place":{"type":"minecraft:randomized_int_state_provider","property":"age",
                    "source":{"type":"minecraft:simple_state_provider","state":{"Name":"fd:colony","Properties":{"age":"0"}}},
                    "values":{"type":"minecraft:uniform","value":{"min_inclusive":0,"max_inclusive":3}}}}},"placement":[]}}}""";
        DataPackFix.Fixed fixed = fix.fix("data/fd/worldgen/configured_feature/patch.json", json.getBytes(StandardCharsets.UTF_8));
        assertEquals("data/fd/worldgen/feature/patch.json", fixed.path());
        JsonObject feature = JsonParser.parseString(new String(fixed.content(), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject inner = feature.getAsJsonObject("config").getAsJsonObject("primary_feature").getAsJsonObject("feature");
        assertEquals("minecraft:simple_block", inner.get("type").getAsString());
        JsonObject provider = inner.getAsJsonObject("to_place");
        assertEquals("minecraft:randomized_int", provider.get("type").getAsString());
        assertEquals("{\"id\":\"fd:colony\",\"properties\":{\"age\":\"0\"}}", provider.get("source").toString());
        assertEquals(3, provider.getAsJsonObject("values").get("max_inclusive").getAsInt());
    }

    @Test
    void itemModelsGetItemDefinitions() {
        // evidence: 26.3 assets/minecraft/items/carrot.json {"model":{"type":"minecraft:model","model":"minecraft:item/carrot"}}
        fix.fix("assets/fd/models/item/cabbage.json", "{}".getBytes(StandardCharsets.UTF_8));
        fix.fix("assets/fd/models/item/tools/knife.json", "{}".getBytes(StandardCharsets.UTF_8));
        fix.fix("assets/fd/models/block/stove.json", "{}".getBytes(StandardCharsets.UTF_8));
        var extras = fix.extras();
        assertEquals(java.util.List.of("assets/fd/items/cabbage.json", "assets/fd/items/tools/knife.json"),
                extras.stream().map(DataPackFix.Fixed::path).toList());
        assertEquals("{\"model\":{\"type\":\"minecraft:model\",\"model\":\"fd:item/cabbage\"}}",
                JsonParser.parseString(new String(extras.get(0).content(), StandardCharsets.UTF_8)).toString());
    }
}

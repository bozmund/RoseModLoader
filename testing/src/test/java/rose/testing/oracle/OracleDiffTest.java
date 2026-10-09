package rose.testing.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.junit.jupiter.api.Test;

class OracleDiffTest {
    private static JsonObject json(String s) {
        return JsonParser.parseString(s.replace('\'', '"')).getAsJsonObject();
    }

    @Test
    void identicalDumpsHaveNoDifferences() {
        JsonObject a = json("{'fd:pot': {'destroy_time': 0.5, 'properties': {'facing': ['north', 'south']}}}");
        assertTrue(OracleDiff.compare("blocks", a, a.deepCopy()).isEmpty());
    }

    @Test
    void missingAndExtraEntries() {
        List<Difference> d = OracleDiff.compare("items", json("{'fd:a': {}, 'fd:c': {}}"), json("{'fd:a': {}, 'fd:b': {}}"));
        assertEquals(2, d.size());
        assertEquals(new Difference("items", "fd:b", "", Difference.Kind.MISSING, new JsonObject(), null), d.get(0));
        assertEquals(Difference.Kind.EXTRA, d.get(1).kind());
        assertEquals("fd:c", d.get(1).key());
    }

    @Test
    void reportsTheDeepestChangedFields() {
        JsonObject rose = json("{'fd:pie': {'components': {'minecraft:food': {'nutrition': 2, 'saturation': 0.5}, 'minecraft:max_stack_size': 64}}}");
        JsonObject ref = json("{'fd:pie': {'components': {'minecraft:food': {'nutrition': 3, 'saturation': 0.5}, 'minecraft:rarity': 'common',"
                + " 'minecraft:max_stack_size': 64}}}");
        List<Difference> d = OracleDiff.compare("items", rose, ref);
        assertEquals(List.of("items fd:pie /components/minecraft:food/nutrition", "items fd:pie /components/minecraft:rarity"),
                d.stream().map(Difference::id).toList());
        assertEquals(Difference.Kind.CHANGED, d.get(0).kind());
        assertEquals(Difference.Kind.MISSING, d.get(1).kind());
    }

    @Test
    void integerAndDecimalFormsOfTheSameNumberAreEqual() {
        assertTrue(OracleDiff.compare("blocks", json("{'fd:x': {'friction': 1}}"), json("{'fd:x': {'friction': 1.0}}")).isEmpty());
    }

    @Test
    void arraysOfDifferentLengthAreOneChange() {
        List<Difference> d = OracleDiff.compare("recipes", json("{'fd:r': {'ingredients': ['a']}}"), json("{'fd:r': {'ingredients': ['a', 'b']}}"));
        assertEquals(1, d.size());
        assertEquals("/ingredients", d.get(0).path());
        assertEquals(Difference.Kind.CHANGED, d.get(0).kind());
    }

    @Test
    void aValueThatCouldNotBeEncodedIsOneDifference() {
        List<Difference> d = OracleDiff.compare("recipes",
                json("{'fd:r': {'type': 'fd:cooking', 'codec': {'$error': 'cannot encode'}}}"),
                json("{'fd:r': {'type': 'fd:cooking', 'codec': {'type': 'fd:cooking', 'cookingtime': 200, 'result': {}}}}"));
        assertEquals(1, d.size());
        assertEquals("recipes fd:r /codec", d.get(0).id());
        assertEquals(Difference.Kind.CHANGED, d.get(0).kind());
    }

    @Test
    void registryAndTagListsCompareAsSets() {
        List<Difference> d = OracleDiff.compare("tags",
                json("{'minecraft:item #c:tools/knife': ['fd:iron_knife', 'fd:gold_knife']}"),
                json("{'minecraft:item #c:tools/knife': ['fd:flint_knife', 'fd:iron_knife']}"));
        assertEquals(List.of("tags minecraft:item #c:tools/knife fd:flint_knife", "tags minecraft:item #c:tools/knife fd:gold_knife"),
                d.stream().map(Difference::id).toList());
        assertEquals(Difference.Kind.MISSING, d.get(0).kind());
        assertEquals(Difference.Kind.EXTRA, d.get(1).kind());
    }
}

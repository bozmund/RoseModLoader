package rose.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoseCliTest {
    @Test
    void keyValueParamsAreTypedLikeJson() {
        JsonObject params = RoseCli.params(new ArrayList<>(List.of(
                "x=6", "y=-60", "face=west", "clear=false", "block=minecraft:oak_stairs[facing=east]", "types=[\"chat\"]")));

        assertEquals(6, params.get("x").getAsInt());
        assertEquals(-60, params.get("y").getAsInt());
        assertEquals("west", params.get("face").getAsString());
        assertTrue(params.get("clear").getAsJsonPrimitive().isBoolean());
        assertEquals("minecraft:oak_stairs[facing=east]", params.get("block").getAsString());
        assertTrue(params.get("types").isJsonArray());
    }

    @Test
    void valuesMayContainEqualsAndSpaces() {
        JsonObject params = RoseCli.params(new ArrayList<>(List.of("command=say a=b c")));
        assertEquals("say a=b c", params.get("command").getAsString());
    }

    @Test
    void paramsOptionIsMergedWithPairs() {
        JsonObject params = RoseCli.params(new ArrayList<>(List.of("--params", "{\"x\": 1}", "y=2")));
        assertEquals(1, params.get("x").getAsInt());
        assertEquals(2, params.get("y").getAsInt());
    }

    @Test
    void rejectsArgumentsWithoutEquals() {
        assertThrows(IllegalArgumentException.class, () -> RoseCli.params(new ArrayList<>(List.of("oops"))));
    }

    @Test
    void optionRemovesNameAndValue() {
        List<String> args = new ArrayList<>(List.of("--target", "server", "world.getBlock"));
        assertEquals("server", RoseCli.option(args, "--target"));
        assertEquals(List.of("world.getBlock"), args);
        assertNull(RoseCli.option(args, "--target"));
    }
}

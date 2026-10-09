package rose.rosetta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StructuralMatcherTest {
    private static Map<String, Set<String>> all(Map<String, Set<String>> appeared) {
        Map<String, Set<String>> all = new HashMap<>(appeared);
        // Common names many classes declare, so they weigh little.
        for (int i = 0; i < 20; i++) all.put("game/Other" + i, Set.of("tick", "use", "getShape"));
        return all;
    }

    @Test
    void renamedClassIsMatchedByItsMembers() {
        Map<String, Set<String>> gone = Map.of(
                "game/FarmBlock", Set.of("turnToDirt", "isNearWater", "shouldMaintainFarmland", "tick", "MOISTURE"));
        Map<String, Set<String>> appeared = Map.of(
                "game/FarmlandBlock", Set.of("turnToDirt", "isNearWater", "shouldMaintainFarmland", "tick", "MOISTURE", "codec"),
                "game/Unrelated", Set.of("tick", "use", "getShape", "explode"));
        assertEquals(Map.of("game/FarmBlock", "game/FarmlandBlock"), StructuralMatcher.match(gone, appeared, all(appeared)));
    }

    @Test
    void ambiguousOrWeakMatchesAreRejected() {
        Map<String, Set<String>> gone = Map.of("game/Old", Set.of("alpha", "beta", "gamma", "tick"));
        // Two equally good candidates: no winner.
        Map<String, Set<String>> twins = Map.of(
                "game/NewA", Set.of("alpha", "beta", "gamma", "tick"),
                "game/NewB", Set.of("alpha", "beta", "gamma", "tick"));
        assertTrue(StructuralMatcher.match(gone, twins, all(twins)).isEmpty());
        // Only common names shared: below the score threshold.
        Map<String, Set<String>> weak = Map.of("game/New", Set.of("tick", "use", "getShape", "delta", "epsilon"));
        assertTrue(StructuralMatcher.match(gone, weak, all(weak)).isEmpty());
    }
}

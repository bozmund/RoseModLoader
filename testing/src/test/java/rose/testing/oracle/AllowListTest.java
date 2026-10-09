package rose.testing.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AllowListTest {
    private static Difference diff(String file, String key, String path) {
        return new Difference(file, key, path, Difference.Kind.CHANGED, null, null);
    }

    @Test
    void globsMatchTheWholeId() {
        AllowList allow = AllowList.parse(List.of(
                "# comment",
                "",
                "items fd:*_knife /components/minecraft:tool*\tthe port retuned knife mining speeds"));
        assertTrue(allow.match(diff("items", "fd:iron_knife", "/components/minecraft:tool/rules")).isPresent());
        assertTrue(allow.match(diff("items", "fd:iron_knife", "/components/minecraft:food")).isEmpty());
        assertTrue(allow.match(diff("blocks", "fd:iron_knife", "/components/minecraft:tool")).isEmpty());
    }

    @Test
    void patternCharactersAreLiteralExceptStar() {
        AllowList allow = AllowList.parse(List.of("tags minecraft:item #c:foods/raw_meat (x)\treason"));
        assertTrue(allow.match(diff("tags", "minecraft:item #c:foods/raw_meat", "(x)")).isPresent());
        assertTrue(allow.match(diff("tags", "minecraft:item #c:foods/raw_meat", "x")).isEmpty());
    }

    @Test
    void everyEntryNeedsAReason() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> AllowList.parse(List.of("items fd:*")));
        assertTrue(e.getMessage().contains("line 1"));
        assertThrows(IllegalArgumentException.class, () -> AllowList.parse(List.of("items fd:*\t  ")));
    }

    @Test
    void reportSeparatesOpenAllowedAndUnusedEntries() {
        AllowList allow = AllowList.parse(List.of("items fd:a*\tport change", "loot_tables *\tnever matches"));
        OracleReport report = new OracleReport("fd", List.of(diff("items", "fd:a", ""), diff("items", "fd:b", "")), allow);
        assertEquals(List.of("items fd:b"), report.open().stream().map(Difference::id).toList());
        String md = report.markdown();
        assertTrue(md.contains("| **total** | **1** | **1** |"), md);
        assertTrue(md.contains("`loot_tables *`: never matches"), md);
    }
}

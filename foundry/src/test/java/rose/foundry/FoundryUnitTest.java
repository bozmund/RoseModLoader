package rose.foundry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FoundryUnitTest {
    private static Task redirectTask() {
        Task t = new Task(Task.State.OPEN, Map.of(), "\n# body\n");
        t.set("id", "mod-1234abcd");
        t.set("kind", "redirect");
        t.set("symbol", "net/minecraft/A.m_1_()V");
        t.set("uses", 3);
        return t;
    }

    @Test
    void taskRoundTripsThroughItsFile(@TempDir Path dir) throws Exception {
        TaskStore store = new TaskStore(dir);
        Task t = redirectTask();
        t.set("ruleLine", "a\tb\tc");
        store.save(t);
        Task read = store.find("mod-1234abcd").orElseThrow();
        assertEquals("redirect", read.kind());
        assertEquals("a\tb\tc", read.get("ruleLine"));
        assertEquals(3, read.getInt("uses", 0));
        assertTrue(read.body().contains("# body"));

        store.move(read, Task.State.LANDED);
        assertEquals(Task.State.LANDED, store.find("mod-1234abcd").orElseThrow().state());
        assertTrue(store.list(Task.State.OPEN).isEmpty());
    }

    @Test
    void scopeGateAllowsOnlyTheTasksFiles() {
        Task t = redirectTask();
        assertTrue(Gates.scope(t, List.of("eras/era-1.20.1/src/main/java/rose/era/v1_20_1/shim/AShim.java",
                "rosetta/rules/forge-1.20.1/redirects.tsv")).passed());
        assertFalse(Gates.scope(t, List.of("analyzer/src/test/java/rose/analyzer/AnalyzerTest.java")).passed());
        assertFalse(Gates.scope(t, List.of("build.gradle.kts")).passed());
        assertFalse(Gates.scope(t, List.of()).passed(), "doing nothing is not a fix");
    }

    @Test
    void analyzeGateNeedsTheSymbolGoneAndNothingNew(@TempDir Path dir) throws Exception {
        Path baseline = report(dir, "base.json", "METHOD_GONE|net/minecraft/A.m_1_()V", "CLASS_GONE|net/minecraft/B");
        Task t = redirectTask();

        assertTrue(Gates.compare(t, report(dir, "fixed.json", "CLASS_GONE|net/minecraft/B"), baseline).passed());
        assertFalse(Gates.compare(t, report(dir, "broken.json", "RULE_BROKEN|net/minecraft/A.m_1_()V", "CLASS_GONE|net/minecraft/B"), baseline).passed());
        assertFalse(Gates.compare(t, report(dir, "regressed.json", "CLASS_GONE|net/minecraft/B", "CLASS_GONE|net/minecraft/C"), baseline).passed());
    }

    @Test
    void plannerWritesExactShimSignatures() {
        assertEquals("public static boolean is(net.minecraft.world.level.block.state.BlockState self, net.minecraft.world.level.block.Block block)",
                Planner.javaSignature("is", "net/minecraft/world/level/block/state/BlockState",
                        "(Lnet/minecraft/world/level/block/Block;)Z", false));
        assertEquals("public static void put(java.lang.String string, int iValue, int iValue2, java.lang.String[] strings)",
                Planner.javaSignature("put", "x/Y", "(Ljava/lang/String;II[Ljava/lang/String;)V", true));
        assertEquals("FoodPropertiesBuilderShim", Planner.shimClassName("net/minecraft/world/food/FoodProperties$Builder"));
    }

    private static Path report(Path dir, String name, String... findings) throws Exception {
        StringBuilder json = new StringBuilder("{\"findings\": [");
        for (int i = 0; i < findings.length; i++) {
            String[] p = findings[i].split("\\|");
            if (i > 0) json.append(',');
            json.append("{\"context\":\"runtime\",\"status\":\"").append(p[0]).append("\",\"symbol\":\"").append(p[1]).append("\"}");
        }
        Path file = dir.resolve(name);
        Files.writeString(file, json.append("]}").toString());
        return file;
    }
}

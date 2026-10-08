package rose.rosetta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;

class NameLayerTest {
    private static NameLayer sample() {
        return new NameLayer("forge-1.20.1",
                Map.of("a/Old", new ClassEntry("a/Old", "b/New", How.INTERMEDIARY),
                        "a/Gone", new ClassEntry("a/Gone", "-", How.GONE)),
                Map.of("m_1_", new MemberEntry("m_1_", "use", "-", How.GONE),
                        "m_2_", new MemberEntry("m_2_", "getShape", "getShape", How.SAME)),
                Map.of("f_1_", new MemberEntry("f_1_", "count", "amount", How.INTERMEDIARY)));
    }

    @Test
    void roundTripsThroughItsFileFormat(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("names.tsv");
        sample().write(file);
        NameLayer read = NameLayer.read(file);

        assertEquals("forge-1.20.1", read.source());
        assertEquals(sample().classes(), read.classes());
        assertEquals(sample().methods(), read.methods());
        assertEquals(sample().fields(), read.fields());
    }

    @Test
    void nestedClassesFollowTheirRenamedOuterClass() {
        assertEquals("b/New", sample().mapClass("a/Old"));
        assertEquals("b/New$Inner", sample().mapClass("a/Old$Inner"));
        assertEquals("c/Unknown", sample().mapClass("c/Unknown"));
    }

    @Test
    void goneMembersReportThatTheyDontExist() {
        assertFalse(sample().method("m_1_").exists());
        assertTrue(sample().method("m_2_").exists());
    }

    @Test
    void descriptorClassesAreRewritten() {
        assertEquals("(Lx/A;I[Lx/B;)Lx/C;", Descriptors.map("(La;I[Lb;)Lc;", n -> "x/" + n.toUpperCase()));
    }

    @Test
    void renameRulesRequireEvidence(@TempDir Path dir) throws IOException {
        Path rules = dir.resolve("rules.tsv");
        Files.writeString(rules, "# comment\na/Old\tb/New\tcorpus: b/New.java\n");
        assertEquals("b/New", RenameRules.read(rules).apply("a/Old"));

        Files.writeString(rules, "a/Old\tb/New\n");
        assertThrows(IOException.class, () -> RenameRules.read(rules));
    }
}

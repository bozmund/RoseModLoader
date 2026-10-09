package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import rose.rosetta.NameLayer;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;

class RefmapRemapperTest {
    private static final NameLayer LAYER = new NameLayer("test",
            Map.of("old/Rabbit", new ClassEntry("old/Rabbit", "new/Rabbit", How.INTERMEDIARY),
                    "old/Stack", new ClassEntry("old/Stack", "new/Stack", How.INTERMEDIARY)),
            Map.of("m_6898_", new MemberEntry("m_6898_", "isFood", "isFood", How.SAME)),
            Map.of("f_1_", new MemberEntry("f_1_", "count", "amount", How.INTERMEDIARY)));
    private final RefmapRemapper remapper = new RefmapRemapper(new RosettaRemapper(LAYER));

    @Test
    void remapsOwnersNamesAndDescriptors() {
        assertEquals("Lnew/Rabbit;isFood(Lnew/Stack;)Z", remapper.remapReference("Lold/Rabbit;m_6898_(Lold/Stack;)Z"));
        assertEquals("Lnew/Stack;amount:I", remapper.remapReference("Lold/Stack;f_1_:I"));
        assertEquals("isFood(Lnew/Stack;)Z", remapper.remapReference("m_6898_(Lold/Stack;)Z"));
        assertEquals("new/Rabbit", remapper.remapReference("old/Rabbit"));
    }

    @Test
    void remapsEveryMappingSection() {
        String json = """
                {"mappings": {"mod/Mixin": {"isFood": "Lold/Rabbit;m_6898_(Lold/Stack;)Z"}},
                 "data": {"searge": {"mod/Mixin": {"isFood": "Lold/Rabbit;m_6898_(Lold/Stack;)Z"}}}}
                """;
        String out = remapper.remap(json);
        String expected = "Lnew/Rabbit;isFood(Lnew/Stack;)Z";
        assertEquals(2, (out.length() - out.replace(expected, "").length()) / expected.length());
    }
}

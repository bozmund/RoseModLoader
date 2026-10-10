package rose.translate.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import rose.rosetta.NameLayer;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;
import rose.translate.ClassIndex;
import rose.translate.RosettaRemapper;

/** Evidence: FD 1.20.1's accesstransformer.cfg (public-f StructureTemplatePool f_210559_ # rawTemplates, ...). */
class AccessTransformerConverterTest {
    private static final NameLayer LAYER = new NameLayer("test",
            Map.of("old/Pool", new ClassEntry("old/Pool", "game/Pool", How.INTERMEDIARY)),
            Map.of("m_1_", new MemberEntry("m_1_", "dispense", "execute", How.INTERMEDIARY)),
            Map.of("f_1_", new MemberEntry("f_1_", "rawTemplates", "rawTemplates", How.SAME),
                    "f_2_", new MemberEntry("f_2_", "removed", "-", How.GONE)));

    private static AccessTransformerConverter converter(Path dir) throws IOException {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "game/Pool", null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "rawTemplates", "Ljava/util/List;", null, null).visitEnd();
        cw.visitMethod(Opcodes.ACC_PROTECTED, "execute", "(Lgame/Pool;)V", null, null).visitEnd();
        cw.visitEnd();
        Path jar = dir.resolve("game.jar");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new ZipEntry("game/Pool.class"));
            out.write(cw.toByteArray());
            out.closeEntry();
        }
        return new AccessTransformerConverter(new RosettaRemapper(LAYER), ClassIndex.of(List.of(jar), true));
    }

    @Test
    void widensClassesFieldsAndMethodsUnderTheir263Names(@TempDir Path dir) throws IOException {
        List<String> report = new ArrayList<>();
        String widener = converter(dir).convert("""
                # FD-style entries
                public-f old.Pool f_1_ # rawTemplates
                public old.Pool m_1_(Lold/Pool;)V
                public-f old.Pool
                """, report);
        assertEquals("""
                accessWidener v2 named
                accessible field game/Pool rawTemplates Ljava/util/List;
                mutable field game/Pool rawTemplates Ljava/util/List;
                accessible method game/Pool execute (Lgame/Pool;)V
                accessible class game/Pool
                extendable class game/Pool
                """, widener);
        assertEquals(List.of(), report);
    }

    @Test
    void reportsWhatCannotCarryOver(@TempDir Path dir) throws IOException {
        List<String> report = new ArrayList<>();
        String widener = converter(dir).convert("""
                public old.Pool f_2_
                public mod.Own
                private old.Pool f_1_
                """, report);
        assertNull(widener);
        assertEquals(3, report.size(), report.toString());
    }
}

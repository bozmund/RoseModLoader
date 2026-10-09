package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import rose.rosetta.NameLayer;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;

class RosettaRemapperTest {
    private static final NameLayer LAYER = new NameLayer("test",
            Map.of("old/Thing", new ClassEntry("old/Thing", "new/Thing", How.INTERMEDIARY)),
            Map.of("m_10_", new MemberEntry("m_10_", "doIt", "doItNow", How.INTERMEDIARY),
                    "m_11_", new MemberEntry("m_11_", "removed", "-", How.GONE)),
            Map.of());

    /** A mod class extending old/Thing, overriding m_10_ and calling m_10_ and m_11_ on old/Thing. */
    private static byte[] modClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/Sub", null, "old/Thing", null);
        MethodVisitor override = cw.visitMethod(Opcodes.ACC_PUBLIC, "m_10_", "(Lold/Thing;)V", null, null);
        override.visitCode();
        override.visitVarInsn(Opcodes.ALOAD, 1);
        override.visitVarInsn(Opcodes.ALOAD, 1);
        override.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "old/Thing", "m_10_", "(Lold/Thing;)V", false);
        override.visitVarInsn(Opcodes.ALOAD, 1);
        override.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "old/Thing", "m_11_", "()V", false);
        override.visitInsn(Opcodes.RETURN);
        override.visitMaxs(0, 0);
        override.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Test
    void renamesClassesOverridesAndCalls() {
        byte[] translated = new JarTranslator(new RosettaRemapper(LAYER)).translateClass(modClass());
        ClassNode node = new ClassNode();
        new ClassReader(translated).accept(node, 0);

        assertEquals("new/Thing", node.superName);
        MethodNode override = node.methods.getFirst();
        assertEquals("doItNow", override.name, "the override follows the method it overrides");
        assertEquals("(Lnew/Thing;)V", override.desc);

        List<String> calls = new ArrayList<>();
        for (var insn = override.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode mi) calls.add(mi.owner + "." + mi.name + mi.desc);
        }
        assertEquals(List.of("new/Thing.doItNow(Lnew/Thing;)V", "new/Thing.removed()V"), calls,
                "a gone member gets its readable old name (era-bridge classes declare it; the analyzer reports it)");
    }

    @Test
    void srgNamesInStringConstantsAreRenamedForReflection() {
        RosettaRemapper remapper = new RosettaRemapper(LAYER);
        assertEquals("doItNow", remapper.mapValue("m_10_"));
        assertEquals("removed", remapper.mapValue("m_11_"), "gone members get their readable old name");
        assertEquals("m_10_ and more", remapper.mapValue("m_10_ and more"), "only whole-string SRG names");
    }
}

package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

class AccessorReplacerTest {
    private static final String DESC = "(Lgame/Type;)Ljava/util/Map;";

    /** {@code @Mixin(game.Manager) interface ManagerAccessor { @Invoker("byType") Map callByType(Type t); @Invoker("kept") void kept(); }} */
    private static byte[] accessor() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, "mod/ManagerAccessor", null, "java/lang/Object", null);
        AnnotationVisitor mixin = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor targets = mixin.visitArray("value");
        targets.visit(null, Type.getObjectType("game/Manager"));
        targets.visitEnd();
        mixin.visitEnd();
        for (String[] m : new String[][] {{"callByType", DESC, "byType"}, {"kept", "()V", "kept"}}) {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, m[0], m[1], null, null);
            AnnotationVisitor invoker = mv.visitAnnotation("Lorg/spongepowered/asm/mixin/gen/Invoker;", true);
            invoker.visit("value", m[2]);
            invoker.visitEnd();
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Test
    void accessorWithARuleBecomesASyntheticDefaultMethodCallingTheHelper() {
        AccessorReplacer replacer = new AccessorReplacer(Map.of("game/Manager.byType", "era/Shim.byType"));
        ClassNode out = new ClassNode();
        new ClassReader(accessor()).accept(replacer.wrap(out), 0);

        MethodNode replaced = out.methods.stream().filter(m -> m.name.equals("callByType")).findFirst().orElseThrow();
        assertEquals(0, replaced.access & Opcodes.ACC_ABSTRACT, "now has a body");
        // Mixin treats an interface with any non-synthetic, non-accessor method as an (unloadable) interface mixin.
        assertTrue((replaced.access & Opcodes.ACC_SYNTHETIC) != 0, "synthetic, so the mixin stays an accessor");
        MethodInsnNode call = (MethodInsnNode) java.util.Arrays.stream(replaced.instructions.toArray())
                .filter(i -> i instanceof MethodInsnNode).findFirst().orElseThrow();
        assertEquals("era/Shim", call.owner);
        assertEquals("(Lgame/Manager;Lgame/Type;)Ljava/util/Map;", call.desc);

        MethodNode kept = out.methods.stream().filter(m -> m.name.equals("kept")).findFirst().orElseThrow();
        assertTrue((kept.access & Opcodes.ACC_ABSTRACT) != 0, "accessors without a rule are left to Mixin");
    }
}

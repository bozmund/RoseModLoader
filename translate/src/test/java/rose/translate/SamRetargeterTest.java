package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

class SamRetargeterTest {
    private static final String OLD = "(Lgame/Barrier;Lgame/Manager;)Ljava/lang/Object;";
    private static final SamRetargeter RETARGETER = new SamRetargeter(Map.of("game/Listener.reload" + OLD, "era/LegacyListener"));

    /** GeckoLib's {@code manager.register(GeckoLibCache::reload)}: a method reference with 1.20.1's reload. */
    @Test
    void lambdasWithTheOldMethodCreateTheLegacyInterface() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "mod/Cache", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "listener", "()Lgame/Listener;", null, null);
        mv.visitCode();
        Handle metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        mv.visitInvokeDynamicInsn("reload", "()Lgame/Listener;", metafactory, Type.getMethodType(OLD),
                new Handle(Opcodes.H_INVOKESTATIC, "mod/Cache", "reload", OLD, false), Type.getMethodType(OLD));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        ClassNode out = new ClassNode();
        new ClassReader(cw.toByteArray()).accept(RETARGETER.wrap(out), 0);
        InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) java.util.Arrays.stream(out.methods.getFirst().instructions.toArray())
                .filter(i -> i instanceof InvokeDynamicInsnNode).findFirst().orElseThrow();
        assertEquals("()Lera/LegacyListener;", indy.desc);
    }

    @Test
    void classesImplementingTheOldMethodImplementTheLegacyInterface() {
        ClassNode out = new ClassNode();
        new ClassReader(implementer("reload", OLD)).accept(RETARGETER.wrap(out), 0);
        assertEquals("era/LegacyListener", out.interfaces.getFirst());
        out = new ClassNode();
        new ClassReader(implementer("reload", "(Lgame/State;)Ljava/lang/Object;")).accept(RETARGETER.wrap(out), 0);
        assertEquals("game/Listener", out.interfaces.getFirst(), "a class with the 26.3 method stays");
    }

    private static byte[] implementer(String name, String desc) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "mod/Loader", null, "java/lang/Object", new String[] {"game/Listener"});
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }
}

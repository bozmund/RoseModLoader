package rose.translate.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class TypedListenerTransformTest {
    private static final String BUS = TypedListenerTransform.EVENT_BUS;
    private static final Handle METAFACTORY = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
            "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
            + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);

    /** {@code static void init(IEventBus bus) { [load something;] bus.addListener(Setup::init); }} */
    private static byte[] modClass(boolean somethingBetween) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "mod/Main", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "init", "(L" + BUS + ";)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInvokeDynamicInsn("accept", "()Ljava/util/function/Consumer;", METAFACTORY,
                Type.getType("(Ljava/lang/Object;)V"),
                new Handle(Opcodes.H_INVOKESTATIC, "mod/Setup", "init", "(Lmod/SetupEvent;)V", false),
                Type.getType("(Lmod/SetupEvent;)V"));
        if (somethingBetween) {
            mv.visitVarInsn(Opcodes.ASTORE, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
        }
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, BUS, "addListener", "(Ljava/util/function/Consumer;)V", true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** "ldc X" and "call owner.name desc" lines of the transformed init method. */
    private static List<String> calls(byte[] original) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(original).accept(new TypedListenerTransform().wrap(writer), 0);
        List<String> out = new ArrayList<>();
        new ClassReader(writer.toByteArray()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitLdcInsn(Object value) {
                        out.add("ldc " + value);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        out.add("call " + name + desc);
                    }
                };
            }
        }, 0);
        return out;
    }

    @Test
    void lambdaEventTypeIsPassedExplicitly() {
        assertEquals(List.of("ldc Lmod/SetupEvent;", "call rose$addListener(Ljava/util/function/Consumer;Ljava/lang/Class;)V"),
                calls(modClass(false)));
    }

    @Test
    void callsWithUnknownLambdaAreLeftAlone() {
        assertEquals(List.of("call addListener(Ljava/util/function/Consumer;)V"), calls(modClass(true)));
    }

    @Test
    void priorityOverloadsGetTypedToo() {
        assertEquals("(Lnet/minecraftforge/eventbus/api/EventPriority;Ljava/util/function/Consumer;Ljava/lang/Class;)V",
                TypedListenerTransform.typedDescriptor(BUS, "addListener",
                        "(Lnet/minecraftforge/eventbus/api/EventPriority;Ljava/util/function/Consumer;)V"));
        assertNull(TypedListenerTransform.typedDescriptor(BUS, "addListener",
                "(Lnet/minecraftforge/eventbus/api/EventPriority;ZLjava/lang/Class;Ljava/util/function/Consumer;)V"));
    }
}

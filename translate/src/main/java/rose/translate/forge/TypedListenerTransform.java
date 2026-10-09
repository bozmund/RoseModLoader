package rose.translate.forge;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import rose.translate.ModTranslator;

/**
 * Forge's {@code IEventBus.addListener(Consumer<T>)} learns the event type {@code T} by inspecting the lambda's
 * class at runtime (TypeTools), which doesn't work on modern JVMs. The type is still in the bytecode: the
 * {@code invokedynamic} that creates the lambda right before the call names it (its instantiated method type).
 * This rewrite passes it along explicitly:
 *
 * <pre>
 * invokedynamic accept()Consumer [... (LFMLCommonSetupEvent;)V]
 * invokeinterface IEventBus.addListener(Consumer)V
 *   becomes
 * invokedynamic accept()Consumer [... (LFMLCommonSetupEvent;)V]
 * ldc FMLCommonSetupEvent.class
 * invokeinterface IEventBus.rose$addListener(Consumer, Class)V
 * </pre>
 *
 * The same applies to the overloads that take a priority (and receiveCancelled) before the consumer.
 */
public final class TypedListenerTransform implements ModTranslator.ExtraTransform {
    public static final String EVENT_BUS = "net/minecraftforge/eventbus/api/IEventBus";
    public static final String TYPED_NAME = "rose$addListener";
    private static final String CONSUMER = "Ljava/util/function/Consumer;";
    private static final String METAFACTORY = "java/lang/invoke/LambdaMetafactory";

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new Rewriter(super.visitMethod(access, name, desc, signature, exceptions));
            }
        };
    }

    /** {@code (…Consumer)V} becomes {@code (…Consumer, Class)V}; {@code null} if the call isn't one we rewrite. */
    static String typedDescriptor(String owner, String name, String desc) {
        if (!owner.equals(EVENT_BUS) || !name.equals("addListener") || !desc.endsWith(CONSUMER + ")V")) return null;
        if (desc.contains("Ljava/lang/Class;")) return null; // already typed
        return desc.substring(0, desc.length() - 2) + "Ljava/lang/Class;)V";
    }

    private static final class Rewriter extends MethodVisitor {
        /** The event type of the Consumer lambda created by the previous instruction, if any. */
        private Type pendingEventType;

        Rewriter(MethodVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... args) {
            super.visitInvokeDynamicInsn(name, desc, bsm, args);
            pendingEventType = null;
            if (bsm.getOwner().equals(METAFACTORY) && Type.getReturnType(desc).getDescriptor().equals(CONSUMER)
                    && args.length >= 3 && args[2] instanceof Type instantiated) {
                Type[] params = instantiated.getArgumentTypes();
                if (params.length == 1 && params[0].getSort() == Type.OBJECT) pendingEventType = params[0];
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
            String typed = typedDescriptor(owner, name, desc);
            if (typed != null && pendingEventType != null) {
                super.visitLdcInsn(pendingEventType);
                super.visitMethodInsn(opcode, owner, TYPED_NAME, typed, isInterface);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            }
            pendingEventType = null;
        }

        // Any other instruction between the lambda and the call means we can't be sure which value is passed.
        @Override public void visitInsn(int opcode) { pendingEventType = null; super.visitInsn(opcode); }
        @Override public void visitIntInsn(int opcode, int operand) { pendingEventType = null; super.visitIntInsn(opcode, operand); }
        @Override public void visitVarInsn(int opcode, int var) { pendingEventType = null; super.visitVarInsn(opcode, var); }
        @Override public void visitTypeInsn(int opcode, String type) { pendingEventType = null; super.visitTypeInsn(opcode, type); }
        @Override public void visitFieldInsn(int opcode, String owner, String name, String desc) { pendingEventType = null; super.visitFieldInsn(opcode, owner, name, desc); }
        @Override public void visitJumpInsn(int opcode, Label label) { pendingEventType = null; super.visitJumpInsn(opcode, label); }
        @Override public void visitLdcInsn(Object value) { pendingEventType = null; super.visitLdcInsn(value); }
        @Override public void visitIincInsn(int var, int increment) { pendingEventType = null; super.visitIincInsn(var, increment); }
        @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) { pendingEventType = null; super.visitTableSwitchInsn(min, max, dflt, labels); }
        @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) { pendingEventType = null; super.visitLookupSwitchInsn(dflt, keys, labels); }
        @Override public void visitMultiANewArrayInsn(String desc, int dims) { pendingEventType = null; super.visitMultiANewArrayInsn(desc, dims); }
    }
}

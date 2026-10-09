package rose.translate;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import rose.rosetta.BridgeRules;

/**
 * Adds {@link BridgeRules bridge methods} to translated mod classes: the methods 26.3 calls that the mod class
 * (written for 1.20.1) doesn't have. Each bridge passes {@code this} and its arguments to a static era helper.
 * Runs after renaming, so rules and the class indexes use 26.3 names.
 */
public final class InheritanceBridger implements ModTranslator.ExtraTransform {
    private final BridgeRules rules;
    private final ClassIndex mod;
    private final ClassIndex game;

    /**
     * @param mod  the mod's translated classes
     * @param game the game, its libraries and Rose's built-in mods
     */
    public InheritanceBridger(BridgeRules rules, ClassIndex mod, ClassIndex game) {
        this.rules = rules;
        this.mod = mod;
        this.game = game;
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            private String name;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                this.name = name;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public void visitEnd() {
                for (BridgeRules.Bridge bridge : bridgesFor(name)) addBridge(this, bridge);
                super.visitEnd();
            }
        };
    }

    /** The bridges class {@code name} gets. */
    List<BridgeRules.Bridge> bridgesFor(String name) {
        List<BridgeRules.Bridge> out = new ArrayList<>();
        ClassIndex.Info info = mod.get(name);
        if (info == null || info.isInterface()) return out;
        for (BridgeRules.Bridge bridge : rules.bridges()) {
            if (needs(info, bridge)) out.add(bridge);
        }
        return out;
    }

    private boolean needs(ClassIndex.Info info, BridgeRules.Bridge bridge) {
        if (info.methods().contains(bridge.method())) return false; // the mod already has it
        if (!ClassIndex.isAssignable(info.name(), bridge.target(), mod, game)) return false;
        if (!bridge.always()) return info.methods().contains(bridge.when());
        if (info.isAbstract()) return false;
        // Only where nothing above already provides it: a concrete vanilla implementation, or a mod superclass that
        // gets the same bridge (or has its own implementation).
        for (ClassIndex.Info c = superOf(info); c != null; c = superOf(c)) {
            if (c.methods().contains(bridge.method()) && !c.abstractMethods().contains(bridge.method())) return false;
            if (mod.get(c.name()) != null && !c.isAbstract() && needs(c, bridge)) return false;
        }
        for (ClassIndex.Info i : ClassIndex.hierarchy(info.name(), mod, game)) {
            if (i.isInterface() && i.methods().contains(bridge.method()) && !i.abstractMethods().contains(bridge.method())) return false;
        }
        return true;
    }

    private ClassIndex.Info superOf(ClassIndex.Info info) {
        if (info.superName() == null) return null;
        ClassIndex.Info s = mod.get(info.superName());
        return s != null ? s : game.get(info.superName());
    }

    private static void addBridge(ClassVisitor cv, BridgeRules.Bridge bridge) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, bridge.methodName(), bridge.methodDesc(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        int slot = 1;
        for (Type arg : Type.getArgumentTypes(bridge.methodDesc())) {
            mv.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), slot);
            slot += arg.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, bridge.helperOwner(), bridge.helperName(), bridge.helperDescriptor(), false);
        mv.visitInsn(Type.getReturnType(bridge.methodDesc()).getOpcode(Opcodes.IRETURN));
        mv.visitMaxs(0, 0); // computed by the writer (COMPUTE_MAXS)
        mv.visitEnd();
    }
}

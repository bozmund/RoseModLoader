package rose.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import rose.rosetta.RedirectRules;
import rose.rosetta.RedirectRules.Redirect;

class RedirectingClassVisitorTest {
    private static final RedirectRules RULES = new RedirectRules(Map.of(
            "game/Modifier.<init>(Ljava/lang/String;D)V",
            new Redirect("game/Modifier.<init>(Ljava/lang/String;D)V", "shim/ModifierShim", "create", "test"),
            "game/Stack.is(Lgame/Item;)Z",
            new Redirect("game/Stack.is(Lgame/Item;)Z", "shim/StackShim", "is", "test")));

    /** {@code Object make(Stack s) { s.is(null); return new Modifier(new Modifier("a", 1).toString(), 2); }} */
    private static byte[] modClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/Maker", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "make", "(Lgame/Stack;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "game/Stack", "is", "(Lgame/Item;)Z", false);
        mv.visitInsn(Opcodes.POP);
        mv.visitTypeInsn(Opcodes.NEW, "game/Modifier");
        mv.visitInsn(Opcodes.DUP);
        mv.visitTypeInsn(Opcodes.NEW, "game/Modifier");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("a");
        mv.visitInsn(Opcodes.DCONST_1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "game/Modifier", "<init>", "(Ljava/lang/String;D)V", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
        mv.visitLdcInsn(2.0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "game/Modifier", "<init>", "(Ljava/lang/String;D)V", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Test
    void constructorsBecomeFactoryCallsAndCallsGoToShims() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(modClass()).accept(new RedirectingClassVisitor(writer, RULES), 0);
        List<String> insns = new ArrayList<>();
        new ClassReader(writer.toByteArray()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        insns.add("new " + type);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        insns.add(owner + "." + name + desc);
                    }
                };
            }
        }, 0);
        assertEquals(List.of(
                "shim/StackShim.is(Lgame/Stack;Lgame/Item;)Z",
                "shim/ModifierShim.create(Ljava/lang/String;D)Lgame/Modifier;",
                "java/lang/Object.toString()Ljava/lang/String;",
                "shim/ModifierShim.create(Ljava/lang/String;D)Lgame/Modifier;"), insns, "no NEW left; nested constructions paired");
    }

    @Test
    void fieldReadsAndWritesGoToTheirOwnShims() {
        RedirectRules rules = new RedirectRules(Map.of(
                "game/Chicken.FOOD:Lgame/Ingredient;", new Redirect("game/Chicken.FOOD:Lgame/Ingredient;", "shim/Food", "chickenFood", "test"),
                "game/Chicken.FOOD=Lgame/Ingredient;", new Redirect("game/Chicken.FOOD=Lgame/Ingredient;", "shim/Food", "setChickenFood", "test"),
                "game/Item.size=I", new Redirect("game/Item.size=I", "shim/Food", "setSize", "test")));
        // static void edit(Item item) { Chicken.FOOD = Chicken.FOOD; item.size = 1; }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/Setup", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "edit", "(Lgame/Item;)V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "game/Chicken", "FOOD", "Lgame/Ingredient;");
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "game/Chicken", "FOOD", "Lgame/Ingredient;");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, "game/Item", "size", "I");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(cw.toByteArray()).accept(new RedirectingClassVisitor(writer, rules), 0);
        List<String> insns = new ArrayList<>();
        new ClassReader(writer.toByteArray()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        insns.add("field " + owner + "." + name);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        insns.add(owner + "." + name + desc);
                    }
                };
            }
        }, 0);
        assertEquals(List.of(
                "shim/Food.chickenFood()Lgame/Ingredient;",
                "shim/Food.setChickenFood(Lgame/Ingredient;)V",
                "shim/Food.setSize(Lgame/Item;I)V"), insns);
    }
}

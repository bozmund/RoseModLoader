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

    /** {@code Function<Stack, ...> f = Stack::is-like unbound reference; Modifier::new}: both handles go to the shims. */
    @Test
    void methodReferencesGoToShims() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/Refs", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "refs", "()V", null, null);
        mv.visitCode();
        org.objectweb.asm.Handle lmf = new org.objectweb.asm.Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        mv.visitInvokeDynamicInsn("test", "()Ljava/util/function/BiPredicate;", lmf,
                org.objectweb.asm.Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Z"),
                new org.objectweb.asm.Handle(Opcodes.H_INVOKEVIRTUAL, "game/Stack", "is", "(Lgame/Item;)Z", false),
                org.objectweb.asm.Type.getType("(Lgame/Stack;Lgame/Item;)Z"));
        mv.visitInsn(Opcodes.POP);
        mv.visitInvokeDynamicInsn("apply", "()Ljava/util/function/BiFunction;", lmf,
                org.objectweb.asm.Type.getType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                new org.objectweb.asm.Handle(Opcodes.H_NEWINVOKESPECIAL, "game/Modifier", "<init>", "(Ljava/lang/String;D)V", false),
                org.objectweb.asm.Type.getType("(Ljava/lang/String;Ljava/lang/Double;)Lgame/Modifier;"));
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        for (RedirectRules rules : List.of(RULES, new RedirectRules(Map.of("game/Stack.is(Lgame/Item;)Z",
                new Redirect("game/Stack.is(Lgame/Item;)Z", "shim/StackShim", "is", "test"))))) {
            ClassWriter writer = new ClassWriter(0);
            new ClassReader(cw.toByteArray()).accept(new RedirectingClassVisitor(writer, rules), 0);
            List<String> handles = new ArrayList<>();
            new ClassReader(writer.toByteArray()).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitInvokeDynamicInsn(String n, String d, org.objectweb.asm.Handle bsm, Object... args) {
                            org.objectweb.asm.Handle h = (org.objectweb.asm.Handle) args[1];
                            handles.add(h.getTag() + " " + h.getOwner() + "." + h.getName() + h.getDesc());
                        }
                    };
                }
            }, 0);
            String constructor = rules == RULES ? Opcodes.H_INVOKESTATIC + " shim/ModifierShim.create(Ljava/lang/String;D)Lgame/Modifier;"
                    : Opcodes.H_NEWINVOKESPECIAL + " game/Modifier.<init>(Ljava/lang/String;D)V";
            assertEquals(List.of(Opcodes.H_INVOKESTATIC + " shim/StackShim.is(Lgame/Stack;Lgame/Item;)Z", constructor), handles);
        }
    }

    @Test
    void typeTestsOfGoneClassesGoToShims() {
        String symbol = "game/Pickaxe." + RedirectRules.INSTANCEOF;
        RedirectRules rules = new RedirectRules(Map.of(symbol, new Redirect(symbol, "shim/ItemShim", "isPickaxe", "test")));
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "mod/Renderer", null, "java/lang/Object", null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "tool", "(Ljava/lang/Object;)Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "game/Pickaxe");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "game/Trident");
        mv.visitInsn(Opcodes.IOR);
        mv.visitInsn(Opcodes.IRETURN);
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
                    public void visitTypeInsn(int opcode, String type) {
                        insns.add("instanceof " + type);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        insns.add(owner + "." + name + desc);
                    }
                };
            }
        }, 0);
        assertEquals(List.of("shim/ItemShim.isPickaxe(Ljava/lang/Object;)Z", "instanceof game/Trident"), insns);
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

    /**
     * mmlib's ItemDrinkBase overrides getFoodProperties ({@code m_41473_}) and calls {@code super.m_41473_()}, which is
     * its parent ItemFoodBase's override: that call stays, while calls reaching the game's method are redirected.
     */
    @Test
    void superCallsToTheModsOwnOverridesStay() throws java.io.IOException {
        String food = "()Lgame/Food;";
        RedirectRules rules = new RedirectRules(Map.of("game/Item.m_41473_" + food,
                new Redirect("game/Item.m_41473_" + food, "shim/ItemShim", "getFoodProperties", "test")));
        byte[] parent = foodItem("mod/FoodItem", "game/Item", Opcodes.INVOKESPECIAL, "game/Item");
        byte[] child = foodItem("mod/DrinkItem", "mod/FoodItem", Opcodes.INVOKESPECIAL, "mod/FoodItem");
        java.nio.file.Path jar = java.nio.file.Files.createTempFile("mod", ".jar");
        try (var out = new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(jar))) {
            for (var e : Map.of("mod/FoodItem.class", parent, "mod/DrinkItem.class", child).entrySet()) {
                out.putNextEntry(new java.util.zip.ZipEntry(e.getKey()));
                out.write(e.getValue());
                out.closeEntry();
            }
        }
        ClassIndex mod = ClassIndex.of(List.of(jar), false);
        java.nio.file.Files.delete(jar);
        assertEquals(List.of("mod/FoodItem.m_41473_" + food), calls(child, rules, mod));
        // FoodItem's own super call reaches the game's Item, which has no such method on 26.3
        assertEquals(List.of("shim/ItemShim.getFoodProperties(Lgame/Item;)Lgame/Food;"), calls(parent, rules, mod));
    }

    /**
     * Nature's Compass' text field overrides EditBox.tick ({@code m_94120_}, gone in 26.3) and its screen calls
     * {@code field.tick()}: that call reaches the mod's override and stays; the override's super call is redirected.
     */
    @Test
    void virtualCallsToTheModsOwnOverridesStay() throws java.io.IOException {
        String food = "()Lgame/Food;";
        RedirectRules rules = new RedirectRules(Map.of("game/Item.m_41473_" + food,
                new Redirect("game/Item.m_41473_" + food, "shim/ItemShim", "getFoodProperties", "test")));
        byte[] item = foodItem("mod/FoodItem", "game/Item", Opcodes.INVOKESPECIAL, "game/Item");
        byte[] caller = foodItem("mod/Caller", "java/lang/Object", Opcodes.INVOKEVIRTUAL, "mod/FoodItem");
        byte[] gameCaller = foodItem("mod/GameCaller", "java/lang/Object", Opcodes.INVOKEVIRTUAL, "game/Item");
        java.nio.file.Path jar = java.nio.file.Files.createTempFile("mod", ".jar");
        try (var out = new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(jar))) {
            out.putNextEntry(new java.util.zip.ZipEntry("mod/FoodItem.class"));
            out.write(item);
            out.closeEntry();
        }
        ClassIndex mod = ClassIndex.of(List.of(jar), false);
        java.nio.file.Files.delete(jar);
        assertEquals(List.of("mod/FoodItem.m_41473_" + food), calls(caller, rules, mod));
        assertEquals(List.of("shim/ItemShim.getFoodProperties(Lgame/Item;)Lgame/Food;"), calls(gameCaller, rules, mod));
    }

    /** {@code class name extends parent { Food m_41473_() { return super.m_41473_(); } }} */
    private static byte[] foodItem(String name, String parent, int opcode, String owner) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, name, null, parent, null);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "m_41473_", "()Lgame/Food;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(opcode, owner, "m_41473_", "()Lgame/Food;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static List<String> calls(byte[] bytes, RedirectRules rules, ClassIndex mod) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(bytes).accept(new RedirectingClassVisitor(writer, rules, mod), 0);
        List<String> insns = new ArrayList<>();
        new ClassReader(writer.toByteArray()).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        insns.add(owner + "." + name + desc);
                    }
                };
            }
        }, 0);
        return insns;
    }
}

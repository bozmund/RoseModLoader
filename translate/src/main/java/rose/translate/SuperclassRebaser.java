package rose.translate;

import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Re-parents mod classes whose vanilla superclass changed shape: {@code class DoughRecipe extends CustomRecipe}
 * becomes {@code extends LegacyCustomRecipe}, an era class that extends the 26.3 class and keeps the 1.20.1
 * constructor and fields. Only the {@code extends} clause and calls to the superclass ({@code super(...)},
 * {@code super.m()}) change; other references to the vanilla class stay as they are.
 *
 * <p>Rules come from {@code rosetta/rules/<source>/superclasses.tsv} ({@code old<TAB>new<TAB>evidence}, 26.3 names).
 */
public final class SuperclassRebaser implements ModTranslator.ExtraTransform {
    private final Map<String, String> rules;

    public SuperclassRebaser(Map<String, String> rules) {
        this.rules = Map.copyOf(rules);
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            private String oldSuper;
            private String newSuper;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                newSuper = superName != null ? rules.get(superName) : null;
                oldSuper = newSuper != null ? superName : null;
                if (newSuper != null && signature != null) signature = signature.replace("L" + oldSuper + ";", "L" + newSuper + ";")
                        .replace("L" + oldSuper + "<", "L" + newSuper + "<");
                super.visit(version, access, name, signature, newSuper != null ? newSuper : superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (newSuper == null) return mv;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String method, String mdesc, boolean isInterface) {
                        // invokespecial on the old superclass: super(...) and super.m(); both now go to the new one.
                        if (opcode == Opcodes.INVOKESPECIAL && owner.equals(oldSuper)) owner = newSuper;
                        super.visitMethodInsn(opcode, owner, method, mdesc, isInterface);
                    }
                };
            }
        };
    }

    public static Map<String, String> read(java.nio.file.Path file) throws java.io.IOException {
        Map<String, String> out = new java.util.LinkedHashMap<>();
        if (!java.nio.file.Files.exists(file)) return out;
        int lineNo = 0;
        for (String line : java.nio.file.Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 3 || p[2].isBlank()) throw new java.io.IOException(file + ":" + lineNo + ": expected old<TAB>new<TAB>evidence");
            out.put(p[0], p[1]);
        }
        return out;
    }
}

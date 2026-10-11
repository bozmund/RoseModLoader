package rose.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Functional interfaces whose single abstract method changed in 26.3 ({@code sam-retargets.tsv}, 26.3 names:
 * {@code interface<TAB>oldMethod(oldDescriptor)<TAB>legacyInterface<TAB>evidence}). The legacy interface (era code)
 * extends the 26.3 one, declares the old method and implements the new one by calling it. A mod's lambda or method
 * reference with the old method, and a mod class implementing the interface with the old method, are pointed at the
 * legacy interface; the object is still the 26.3 interface wherever it is passed.
 */
public final class SamRetargeter implements ModTranslator.ExtraTransform {
    /** {@code interface + "." + name + descriptor} (the old method) to the legacy interface. */
    private final Map<String, String> rules;

    public SamRetargeter(Map<String, String> rules) {
        this.rules = Map.copyOf(rules);
    }

    public static Map<String, String> read(Path file) throws IOException {
        Map<String, String> out = new LinkedHashMap<>();
        if (!Files.exists(file)) return out;
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 4 || p[3].isBlank()) {
                throw new IOException(file + ":" + lineNo + ": expected interface<TAB>oldMethod<TAB>legacyInterface<TAB>evidence");
            }
            out.put(p[0] + "." + p[1], p[2]);
        }
        return out;
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        if (rules.isEmpty()) return next;
        return new ClassNode(Opcodes.ASM9) {
            @Override
            public void visitEnd() {
                retarget(this);
                accept(next);
            }
        };
    }

    void retarget(ClassNode node) {
        if (node.interfaces != null) {
            for (int i = 0; i < node.interfaces.size(); i++) {
                String iface = node.interfaces.get(i);
                String legacy = implementedRule(node, iface);
                if (legacy == null) continue;
                node.interfaces.set(i, legacy);
                if (node.signature != null) node.signature = node.signature.replace("L" + iface + ";", "L" + legacy + ";");
            }
        }
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof InvokeDynamicInsnNode indy) || !isMetafactory(indy.bsm)
                        || indy.bsmArgs.length == 0 || !(indy.bsmArgs[0] instanceof Type sam)) continue;
                Type created = Type.getReturnType(indy.desc);
                if (created.getSort() != Type.OBJECT) continue;
                String legacy = rules.get(created.getInternalName() + "." + indy.name + sam.getDescriptor());
                if (legacy != null) {
                    indy.desc = indy.desc.substring(0, indy.desc.indexOf(')') + 1) + "L" + legacy + ";";
                }
            }
        }
    }

    /** The legacy interface when {@code node} implements {@code iface} with its old method, else null. */
    private String implementedRule(ClassNode node, String iface) {
        for (var rule : rules.entrySet()) {
            if (!rule.getKey().startsWith(iface + ".")) continue;
            String method = rule.getKey().substring(iface.length() + 1);
            for (MethodNode m : node.methods) {
                if ((m.name + m.desc).equals(method) && (m.access & Opcodes.ACC_ABSTRACT) == 0) return rule.getValue();
            }
        }
        return null;
    }

    private static boolean isMetafactory(Handle bootstrap) {
        return bootstrap.getOwner().equals("java/lang/invoke/LambdaMetafactory");
    }
}

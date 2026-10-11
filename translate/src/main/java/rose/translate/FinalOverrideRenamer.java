package rose.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Methods that 26.3 made final while old mods override them ({@code final-overrides.tsv}, 26.3 names:
 * {@code target<TAB>method(descriptor)<TAB>legacyName<TAB>legacyInterface<TAB>evidence}). Renamed to 26.3 names, a mod
 * class's old override lands on the final method, and the class doesn't link. The override is renamed to
 * {@code legacyName} and its class implements {@code legacyInterface} (era code), through which a hook in the final
 * method calls it. Mod {@code super} calls that reached a mod override follow the rename. Runs after renaming.
 */
public final class FinalOverrideRenamer implements ModTranslator.ExtraTransform {
    public record Rule(String target, String name, String desc, String legacyName, String legacyInterface) {}

    private final List<Rule> rules;
    private final ClassIndex mod;
    private final ClassIndex game;

    /**
     * @param mod  the mod's translated classes
     * @param game the game, its libraries and Rose's built-in mods
     */
    public FinalOverrideRenamer(List<Rule> rules, ClassIndex mod, ClassIndex game) {
        this.rules = List.copyOf(rules);
        this.mod = mod;
        this.game = game;
    }

    public static List<Rule> read(Path file) throws IOException {
        List<Rule> out = new ArrayList<>();
        if (!Files.exists(file)) return out;
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 5 || p[4].isBlank() || !p[1].contains("(")) {
                throw new IOException(file + ":" + lineNo + ": expected target<TAB>method(desc)<TAB>legacyName<TAB>legacyInterface<TAB>evidence");
            }
            int paren = p[1].indexOf('(');
            out.add(new Rule(p[0], p[1].substring(0, paren), p[1].substring(paren), p[2], p[3]));
        }
        return out;
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        if (rules.isEmpty()) return next;
        return new ClassNode(Opcodes.ASM9) {
            @Override
            public void visitEnd() {
                rename(this);
                accept(next);
            }
        };
    }

    void rename(ClassNode node) {
        for (Rule rule : rules) {
            for (MethodNode method : node.methods) {
                if (!method.name.equals(rule.name()) || !method.desc.equals(rule.desc())) continue;
                if (!ClassIndex.isAssignable(node.name, rule.target(), mod, game)) continue;
                method.name = rule.legacyName();
                if (node.interfaces == null) node.interfaces = new ArrayList<>();
                if (!node.interfaces.contains(rule.legacyInterface())) node.interfaces.add(rule.legacyInterface());
            }
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals(rule.name())
                            && call.desc.equals(rule.desc()) && modDeclares(call.owner, rule)) {
                        call.name = rule.legacyName();
                    }
                }
            }
        }
    }

    /** Whether {@code owner} or a mod superclass of it declares the overridden method (which is renamed). */
    private boolean modDeclares(String owner, Rule rule) {
        for (ClassIndex.Info c = mod.get(owner); c != null; c = c.superName() == null ? null : mod.get(c.superName())) {
            if (c.methods().contains(rule.name() + rule.desc())) return ClassIndex.isAssignable(c.name(), rule.target(), mod, game);
        }
        return false;
    }
}

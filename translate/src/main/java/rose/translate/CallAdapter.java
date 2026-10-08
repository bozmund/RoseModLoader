package rose.translate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Adapts calls whose method still exists by name but now has a <em>wider</em> compiled signature, most often
 * because 26.x made it generic: {@code BlockState.is(Block)} became {@code TypedInstance.is(T)}, which compiles to
 * {@code is(Object)}. The old call works unchanged if its descriptor is updated (plus a cast when the new method
 * returns a wider type, or a pop when it now returns a value the old code didn't expect).
 *
 * <p>Deliberately strict: same name, same static-ness, same number of parameters, every old argument assignable to
 * the new parameter, and <b>exactly one</b> such candidate in the class hierarchy. Anything else is left for a
 * rule or a shim.
 */
public final class CallAdapter {
    /**
     * @param newDesc the descriptor to call instead
     * @param castTo  internal name (or array descriptor) to CHECKCAST the result to, or {@code null}
     * @param pop     the new method returns a value the old caller didn't expect; discard it
     */
    public record Adaptation(String newDesc, String castTo, boolean pop) {}

    private final ClassIndex first;
    private final ClassIndex fallback;

    /** @param first the mod's own (translated) classes; {@code fallback} the game and its libraries */
    public CallAdapter(ClassIndex first, ClassIndex fallback) {
        this.first = first;
        this.fallback = fallback;
    }

    /** Whether {@code owner.name desc} exists (possibly inherited). */
    public boolean exists(String owner, String name, String desc) {
        String key = name + desc;
        return ClassIndex.hierarchy(owner, first, fallback).stream().anyMatch(i -> i.methods().contains(key));
    }

    /** The unique adaptation for a call that doesn't resolve as written, or {@code null}. Names are 26.3 names. */
    public Adaptation find(String owner, String name, String desc, boolean isStatic) {
        if (name.startsWith("<")) return null;
        Set<String> candidates = new LinkedHashSet<>();
        for (ClassIndex.Info info : ClassIndex.hierarchy(owner, first, fallback)) {
            for (String m : info.methods()) {
                if (!m.startsWith(name + "(") || m.length() == name.length()) continue;
                if (info.staticMethods().contains(m) != isStatic) continue;
                candidates.add(m.substring(name.length()));
            }
        }
        candidates.remove(desc);
        List<Adaptation> applicable = new ArrayList<>();
        for (String candidate : candidates) {
            Adaptation a = applicable(desc, candidate);
            if (a != null) applicable.add(a);
        }
        return applicable.size() == 1 ? applicable.getFirst() : null;
    }

    private Adaptation applicable(String oldDesc, String newDesc) {
        Type[] oldArgs = Type.getArgumentTypes(oldDesc);
        Type[] newArgs = Type.getArgumentTypes(newDesc);
        if (oldArgs.length != newArgs.length) return null;
        for (int i = 0; i < oldArgs.length; i++) {
            if (!assignable(oldArgs[i], newArgs[i])) return null;
        }
        Type expected = Type.getReturnType(oldDesc);
        Type actual = Type.getReturnType(newDesc);
        if (expected.getSort() == Type.VOID) {
            return new Adaptation(newDesc, null, actual.getSort() != Type.VOID);
        }
        if (isPrimitive(expected) || isPrimitive(actual)) {
            return expected.equals(actual) ? new Adaptation(newDesc, null, false) : null;
        }
        if (assignable(actual, expected)) return new Adaptation(newDesc, null, false);
        if (assignable(expected, actual)) {
            return new Adaptation(newDesc, expected.getSort() == Type.ARRAY ? expected.getDescriptor() : expected.getInternalName(), false);
        }
        return null;
    }

    private boolean assignable(Type from, Type to) {
        if (from.equals(to)) return true;
        if (isPrimitive(from) || isPrimitive(to)) return false;
        if (to.getDescriptor().equals("Ljava/lang/Object;")) return true;
        if (from.getSort() == Type.ARRAY || to.getSort() == Type.ARRAY) return false;
        return ClassIndex.isAssignable(from.getInternalName(), to.getInternalName(), first, fallback);
    }

    private static boolean isPrimitive(Type t) {
        return t.getSort() != Type.OBJECT && t.getSort() != Type.ARRAY;
    }

    /** A class visitor (run after renaming) that rewrites adaptable calls. */
    public ClassVisitor visitor(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean itf) {
                        Adaptation a = opcode == Opcodes.INVOKESPECIAL || owner.startsWith("[") || exists(owner, method, desc)
                                ? null : find(owner, method, desc, opcode == Opcodes.INVOKESTATIC);
                        if (a == null) {
                            super.visitMethodInsn(opcode, owner, method, desc, itf);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, method, a.newDesc(), itf);
                        if (a.castTo() != null) super.visitTypeInsn(Opcodes.CHECKCAST, a.castTo());
                        if (a.pop()) super.visitInsn(Type.getReturnType(a.newDesc()).getSize() == 2 ? Opcodes.POP2 : Opcodes.POP);
                    }
                };
            }
        };
    }
}

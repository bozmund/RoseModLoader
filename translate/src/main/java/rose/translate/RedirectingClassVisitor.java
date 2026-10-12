package rose.translate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import rose.rosetta.RedirectRules;

/**
 * Replaces calls that have a {@link RedirectRules redirect rule} with a static call to the rule's shim. Runs on the
 * mod's <em>old</em> names, before renaming, so rules can use the symbols exactly as the analyzer reports them.
 *
 * <p>A type test rule turns {@code value instanceof Owner} into {@code Shim.name(value)}.
 *
 * <p>A constructor rule ({@code owner.<init>(args)V}) turns {@code new Owner(args)} into a static factory call
 * {@code Shim.name(args)} that returns the object: the {@code NEW}/{@code DUP} pair is removed.
 *
 * <p>Method references ({@code Owner::method}, {@code Owner::new}) to a redirected member become references to the
 * shim: the handle in the {@code invokedynamic}'s bootstrap arguments is swapped for a static one.
 *
 * <p>A {@code super} call to a method a mod class declares (an override of a redirected game method) stays a call to
 * the mod's method: the rule is for the game's method, which the mod's override replaces.
 */
public final class RedirectingClassVisitor extends ClassVisitor {
    private final RedirectRules rules;
    private final ClassIndex modClasses;

    public RedirectingClassVisitor(ClassVisitor next, RedirectRules rules) {
        this(next, rules, null);
    }

    /** @param modClasses the mod's classes in their old names, to keep super calls to its overrides; may be null */
    public RedirectingClassVisitor(ClassVisitor next, RedirectRules rules, ClassIndex modClasses) {
        super(Opcodes.ASM9, next);
        this.rules = rules;
        this.modClasses = modClasses;
    }

    /**
     * The rule for a call, or {@code null}. {@code super} and virtual calls that reach the mod's own method (its
     * override of a game method that a rule redirects) have none: the mod's method is still there to call.
     */
    private RedirectRules.Redirect ruleFor(int opcode, String owner, String name, String desc) {
        if ((opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEVIRTUAL) && modDeclares(owner, name + desc)) return null;
        return rules.find(owner, name, desc);
    }

    private boolean modDeclares(String owner, String method) {
        if (modClasses == null) return false;
        for (ClassIndex.Info c = modClasses.get(owner); c != null; c = c.superName() == null ? null : modClasses.get(c.superName())) {
            if (c.methods().contains(method) && !c.abstractMethods().contains(method)) return true;
        }
        return false;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor next = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (rules.hasConstructorRules()) {
            // Constructor redirects need to see the NEW that belongs to each <init>, so buffer the whole method.
            return new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions) {
                @Override
                public void visitEnd() {
                    redirectConstructors(this);
                    redirectCalls(this);
                    accept(next);
                }
            };
        }
        return new MethodVisitor(Opcodes.ASM9, next) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean isInterface) {
                RedirectRules.Redirect rule = method.equals("<init>") ? null : ruleFor(opcode, owner, method, desc);
                if (rule == null) {
                    super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    return;
                }
                // Same operands on the stack: the receiver (if any) becomes the shim's first argument.
                super.visitMethodInsn(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                        rule.shimDescriptor(opcode == Opcodes.INVOKESTATIC), false);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                RedirectRules.Redirect rule = opcode == Opcodes.INSTANCEOF ? rules.findInstanceOf(type) : null;
                if (rule == null) {
                    super.visitTypeInsn(opcode, type);
                    return;
                }
                super.visitMethodInsn(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(), rule.shimDescriptor(true), false);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrap, Object... args) {
                super.visitInvokeDynamicInsn(name, desc, bootstrap, redirectHandles(args));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String field, String desc) {
                RedirectRules.Redirect rule = rules.findField(owner, field, desc, !isRead(opcode));
                if (rule == null) {
                    super.visitFieldInsn(opcode, owner, field, desc);
                    return;
                }
                super.visitMethodInsn(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                        rule.shimDescriptor(isStatic(opcode)), false);
            }
        };
    }

    private static boolean isRead(int opcode) {
        return opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD;
    }

    private static boolean isStatic(int opcode) {
        return opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
    }

    /** Bootstrap arguments with handles to redirected methods and constructors pointing at their shims. */
    private Object[] redirectHandles(Object[] args) {
        Object[] out = args;
        for (int i = 0; i < args.length; i++) {
            if (!(args[i] instanceof Handle handle)) continue;
            Handle redirected = redirect(handle);
            if (redirected == handle) continue;
            if (out == args) out = args.clone();
            out[i] = redirected;
        }
        return out;
    }

    private Handle redirect(Handle handle) {
        int tag = handle.getTag();
        boolean constructor = tag == Opcodes.H_NEWINVOKESPECIAL;
        boolean call = tag == Opcodes.H_INVOKESTATIC || tag == Opcodes.H_INVOKEVIRTUAL || tag == Opcodes.H_INVOKEINTERFACE;
        if (!constructor && !call) return handle;
        RedirectRules.Redirect rule = rules.find(handle.getOwner(), handle.getName(), handle.getDesc());
        if (rule == null || (constructor != handle.getName().equals("<init>"))) return handle;
        return new Handle(Opcodes.H_INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                rule.shimDescriptor(constructor || tag == Opcodes.H_INVOKESTATIC), false);
    }

    private void redirectCalls(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof org.objectweb.asm.tree.InvokeDynamicInsnNode indy) {
                indy.bsmArgs = redirectHandles(indy.bsmArgs);
                continue;
            }
            if (insn instanceof TypeInsnNode type && type.getOpcode() == Opcodes.INSTANCEOF) {
                RedirectRules.Redirect rule = rules.findInstanceOf(type.desc);
                if (rule != null) {
                    method.instructions.set(type, new MethodInsnNode(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                            rule.shimDescriptor(true), false));
                }
                continue;
            }
            if (insn instanceof org.objectweb.asm.tree.FieldInsnNode field) {
                RedirectRules.Redirect rule = rules.findField(field.owner, field.name, field.desc, !isRead(field.getOpcode()));
                if (rule != null) {
                    method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                            rule.shimDescriptor(isStatic(field.getOpcode())), false));
                }
                continue;
            }
            if (!(insn instanceof MethodInsnNode call) || call.name.equals("<init>")) continue;
            RedirectRules.Redirect rule = ruleFor(call.getOpcode(), call.owner, call.name, call.desc);
            if (rule == null) continue;
            boolean isStatic = call.getOpcode() == Opcodes.INVOKESTATIC;
            method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                    rule.shimDescriptor(isStatic), false));
        }
    }

    /**
     * Pairs each {@code INVOKESPECIAL X.<init>} with the latest unmatched {@code NEW X} (javac nests them, so the
     * innermost construction completes first) and rewrites the ones that have a rule.
     */
    private void redirectConstructors(MethodNode method) {
        Map<String, Deque<TypeInsnNode>> pending = new HashMap<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
            AbstractInsnNode nextInsn = insn.getNext();
            if (insn instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW) {
                pending.computeIfAbsent(type.desc, k -> new ArrayDeque<>()).push(type);
            } else if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("<init>")) {
                Deque<TypeInsnNode> news = pending.get(call.owner);
                TypeInsnNode created = news != null ? news.poll() : null; // null: a super()/this() call
                RedirectRules.Redirect rule = created != null ? rules.find(call.owner, call.name, call.desc) : null;
                if (rule != null && created.getNext() != null && created.getNext().getOpcode() == Opcodes.DUP
                        && !hasFrameBetween(created, call)) {
                    method.instructions.remove(created.getNext());
                    method.instructions.remove(created);
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                            rule.shimDescriptor(true), false));
                }
            }
            insn = nextInsn;
        }
    }

    /** A stack map frame inside the argument code would refer to the removed NEW; leave such calls alone. */
    private static boolean hasFrameBetween(AbstractInsnNode from, AbstractInsnNode to) {
        for (AbstractInsnNode i = from; i != null && i != to; i = i.getNext()) {
            if (i instanceof org.objectweb.asm.tree.FrameNode) return true;
        }
        return false;
    }
}

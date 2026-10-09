package rose.translate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
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
 * <p>A constructor rule ({@code owner.<init>(args)V}) turns {@code new Owner(args)} into a static factory call
 * {@code Shim.name(args)} that returns the object: the {@code NEW}/{@code DUP} pair is removed.
 */
public final class RedirectingClassVisitor extends ClassVisitor {
    private final RedirectRules rules;

    public RedirectingClassVisitor(ClassVisitor next, RedirectRules rules) {
        super(Opcodes.ASM9, next);
        this.rules = rules;
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
                RedirectRules.Redirect rule = opcode == Opcodes.INVOKESPECIAL ? null : rules.find(owner, method, desc);
                if (rule == null) {
                    super.visitMethodInsn(opcode, owner, method, desc, isInterface);
                    return;
                }
                // Same operands on the stack: the receiver (if any) becomes the shim's first argument.
                super.visitMethodInsn(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                        rule.shimDescriptor(opcode == Opcodes.INVOKESTATIC), false);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String field, String desc) {
                RedirectRules.Redirect rule = isRead(opcode) ? rules.findField(owner, field, desc) : null;
                if (rule == null) {
                    super.visitFieldInsn(opcode, owner, field, desc);
                    return;
                }
                super.visitMethodInsn(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                        rule.shimDescriptor(opcode == Opcodes.GETSTATIC), false);
            }
        };
    }

    private static boolean isRead(int opcode) {
        return opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD;
    }

    private void redirectCalls(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof org.objectweb.asm.tree.FieldInsnNode field && isRead(field.getOpcode())) {
                RedirectRules.Redirect rule = rules.findField(field.owner, field.name, field.desc);
                if (rule != null) {
                    method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, rule.shimOwner(), rule.shimName(),
                            rule.shimDescriptor(field.getOpcode() == Opcodes.GETSTATIC), false));
                }
                continue;
            }
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() == Opcodes.INVOKESPECIAL) continue;
            RedirectRules.Redirect rule = rules.find(call.owner, call.name, call.desc);
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

package rose.translate;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import rose.rosetta.RedirectRules;

/**
 * Replaces calls that have a {@link RedirectRules redirect rule} with a static call to the rule's shim. Runs on the
 * mod's <em>old</em> names, before renaming, so rules can use the symbols exactly as the analyzer reports them.
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
        };
    }
}

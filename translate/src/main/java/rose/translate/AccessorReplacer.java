package rose.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Mixin accessors ({@code @Accessor}/{@code @Invoker} interface methods) whose vanilla target no longer exists
 * would fail the whole accessor mixin. For targets with a rule ({@code accessors.tsv}: {@code owner.member<TAB>helper
 * <TAB>evidence}, 26.3 names) the abstract method becomes a default method calling the era helper with
 * {@code (Owner) this}; the mixin then only adds the interface, and callers keep working.
 */
public final class AccessorReplacer implements ModTranslator.ExtraTransform {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
    private static final String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";

    private final Map<String, String> rules;

    /** @param rules {@code owner.member -> helperOwner.helperName} */
    public AccessorReplacer(Map<String, String> rules) {
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
            if (p.length < 3 || p[2].isBlank()) throw new IOException(file + ":" + lineNo + ": expected owner.member<TAB>helper<TAB>evidence");
            out.put(p[0], p[1]);
        }
        return out;
    }

    @Override
    public ClassVisitor wrap(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            private boolean isInterface;
            private final List<String> targets = new ArrayList<>();

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                AnnotationVisitor av = super.visitAnnotation(desc, visible);
                if (!desc.equals(MIXIN)) return av;
                return new AnnotationVisitor(Opcodes.ASM9, av) {
                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        AnnotationVisitor array = super.visitArray(name);
                        if (!name.equals("value")) return array;
                        return new AnnotationVisitor(Opcodes.ASM9, array) {
                            @Override
                            public void visit(String n, Object value) {
                                if (value instanceof Type t) targets.add(t.getInternalName());
                                super.visit(n, value);
                            }
                        };
                    }
                };
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (!isInterface || (access & Opcodes.ACC_ABSTRACT) == 0 || targets.isEmpty()) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
                // Buffer the method to see its annotation before writing it.
                String[] thrown = exceptions;
                return new MethodNode(Opcodes.ASM9, access, name, desc, signature, exceptions) {
                    @Override
                    public void visitEnd() {
                        String helper = helperFor(this);
                        if (helper == null) {
                            accept(cv);
                            return;
                        }
                        // Synthetic: Mixin still classes the interface as an accessor mixin (those may be loaded
                        // directly); any other non-accessor method makes it an interface mixin, which may not.
                        MethodVisitor mv = cv.visitMethod((access & ~Opcodes.ACC_ABSTRACT) | Opcodes.ACC_SYNTHETIC, name, desc, signature, thrown);
                        writeDefault(mv, targets.getFirst(), desc, helper);
                    }
                };
            }

            private String helperFor(MethodNode method) {
                if (method.visibleAnnotations == null) return null;
                for (AnnotationNode a : method.visibleAnnotations) {
                    if (!a.desc.equals(ACCESSOR) && !a.desc.equals(INVOKER)) continue;
                    String member = a.values != null && a.values.size() >= 2 && a.values.get(0).equals("value") ? (String) a.values.get(1) : null;
                    if (member == null) continue;
                    for (String target : targets) {
                        String helper = rules.get(target + "." + member);
                        if (helper != null) return helper;
                    }
                }
                return null;
            }
        };
    }

    /** {@code default R m(args) { return Helper.name((Target) this, args); }} */
    private static void writeDefault(MethodVisitor mv, String target, String desc, String helper) {
        int dot = helper.lastIndexOf('.');
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, target);
        int slot = 1;
        for (Type arg : Type.getArgumentTypes(desc)) {
            mv.visitVarInsn(arg.getOpcode(Opcodes.ILOAD), slot);
            slot += arg.getSize();
        }
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, helper.substring(0, dot), helper.substring(dot + 1), "(L" + target + ";" + desc.substring(1), false);
        mv.visitInsn(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

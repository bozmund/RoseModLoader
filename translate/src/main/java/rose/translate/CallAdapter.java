package rose.translate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.signature.SignatureVisitor;
import rose.rosetta.ConversionRules;
import rose.rosetta.ConversionRules.Conversion;

/**
 * Adapts calls whose method still exists by name but now has a different compiled signature:
 * <ul>
 *   <li><b>wider</b> parameters, most often because 26.x made the method generic: {@code BlockState.is(Block)}
 *       became {@code TypedInstance.is(T)}, which compiles to {@code is(Object)}. The descriptor is updated (plus a
 *       cast when the new method returns a wider type, or a pop when it now returns a value the old code ignored);</li>
 *   <li><b>converted</b> types from {@link ConversionRules}: {@code new MobEffectInstance(MobEffect, int)} became
 *       {@code (Holder<MobEffect>, int)}. The call goes through a generated static trampoline in the calling class
 *       that converts arguments and the result.</li>
 * </ul>
 * Fields whose type changed the same way ({@code MobEffects.SPEED} is a {@code Holder<MobEffect>} now) are read
 * through the conversion too.
 *
 * <p>Deliberately strict: same name, same static-ness, same number of parameters, every old argument assignable
 * (or convertible) to the new parameter, and <b>exactly one</b> such candidate in the class hierarchy. Anything else
 * is left for a rule or a shim.
 */
public final class CallAdapter {
    /**
     * @param newDesc            the descriptor to call instead
     * @param castTo             internal name (or array descriptor) to CHECKCAST the result to, or {@code null}
     * @param pop                the new method returns a value the old caller didn't expect; discard it
     * @param argConversions     per argument, the conversion to apply, or {@code null}; {@code null} list if none
     * @param returnConversion   conversion of the result to the old return type, or {@code null}
     * @param swap               the method's two parameters swapped places ({@code (Properties, WoodType)} became
     *                           {@code (WoodType, Properties)}); a SWAP before the call fixes the order
     */
    public record Adaptation(String newDesc, String castTo, boolean pop, List<Conversion> argConversions, Conversion returnConversion,
                             boolean swap) {
        public Adaptation(String newDesc, String castTo, boolean pop) {
            this(newDesc, castTo, pop, null, null, false);
        }

        public Adaptation(String newDesc, String castTo, boolean pop, List<Conversion> argConversions, Conversion returnConversion) {
            this(newDesc, castTo, pop, argConversions, returnConversion, false);
        }

        public boolean needsTrampoline() {
            return argConversions != null || returnConversion != null;
        }
    }

    /** A field read or write with a changed type, converted on the way. */
    public record FieldAdaptation(String newDesc, Conversion conversion, String castTo) {}

    private final ClassIndex first;
    private final ClassIndex fallback;
    private final ConversionRules conversions;

    /** @param first the mod's own (translated) classes; {@code fallback} the game and its libraries */
    public CallAdapter(ClassIndex first, ClassIndex fallback) {
        this(first, fallback, ConversionRules.empty());
    }

    public CallAdapter(ClassIndex first, ClassIndex fallback, ConversionRules conversions) {
        this.first = first;
        this.fallback = fallback;
        this.conversions = conversions;
    }

    /** Whether {@code owner.name desc} exists (possibly inherited). */
    public boolean exists(String owner, String name, String desc) {
        String key = name + desc;
        if (name.equals("<init>")) { // constructors aren't inherited
            ClassIndex.Info info = first.get(owner);
            if (info == null) info = fallback.get(owner);
            return info == null || info.methods().contains(key);
        }
        return ClassIndex.hierarchy(owner, first, fallback).stream().anyMatch(i -> i.methods().contains(key));
    }

    /** The unique adaptation for a call that doesn't resolve as written, or {@code null}. Names are 26.3 names. */
    public Adaptation find(String owner, String name, String desc, boolean isStatic) {
        boolean constructor = name.equals("<init>");
        // descriptor -> generic signature (or "" when it has none); first declaration in the hierarchy wins
        Map<String, String> candidates = new LinkedHashMap<>();
        List<ClassIndex.Info> hierarchy = ClassIndex.hierarchy(owner, first, fallback);
        for (ClassIndex.Info info : constructor ? hierarchy.subList(0, Math.min(1, hierarchy.size())) : hierarchy) {
            for (String m : info.methods()) {
                if (!m.startsWith(name + "(")) continue;
                if (info.staticMethods().contains(m) != isStatic) continue;
                candidates.putIfAbsent(m.substring(name.length()), info.signatures().getOrDefault(m, ""));
            }
        }
        candidates.remove(desc);
        List<Adaptation> applicable = new ArrayList<>();
        for (var candidate : candidates.entrySet()) {
            Adaptation a = applicable(desc, candidate.getKey(), candidate.getValue(), constructor);
            if (a != null) applicable.add(a);
        }
        if (applicable.size() == 1) return applicable.getFirst();
        // Like Java's overload resolution: an overload reached by widening alone beats ones needing conversions
        // (playSound(Player, ..., SoundEvent, ...) picks the SoundEvent overload, not the Holder one).
        List<Adaptation> plain = applicable.stream().filter(a -> !a.needsTrampoline() && !a.swap()).toList();
        return plain.size() == 1 ? plain.getFirst() : null;
    }

    /** True if a generic method signature's return type is a type variable (e.g. {@code (TT;)TT;}). */
    static boolean returnsTypeVariable(String signature) {
        return !signature.isEmpty() && signature.substring(signature.lastIndexOf(')') + 1).startsWith("T");
    }

    /**
     * Return types must stay compatible <em>in meaning</em>, not just for the verifier:
     * <ul>
     *   <li>same type; or the old call ignored nothing and the new method returns a value (discarded);</li>
     *   <li>a narrower (covariant) type, unless the old type was {@code Object} (then the caller casts it to what the
     *       old generic meant, and a different object would fail at runtime);</li>
     *   <li>a wider type only when the new method's return is a generic type variable (erasure), so casting back is
     *       safe;</li>
     *   <li>a type with a conversion back to the old one.</li>
     * </ul>
     */
    private Adaptation applicable(String oldDesc, String newDesc, String signature, boolean constructor) {
        Type[] oldArgs = Type.getArgumentTypes(oldDesc);
        Type[] newArgs = Type.getArgumentTypes(newDesc);
        if (oldArgs.length != newArgs.length) return null;
        if (isSwap(oldArgs, newArgs) && Type.getReturnType(oldDesc).equals(Type.getReturnType(newDesc))) {
            return new Adaptation(newDesc, null, false, null, null, true);
        }
        List<String> typeArgs = paramTypeArguments(signature, newArgs.length);
        Conversion[] argConversions = new Conversion[oldArgs.length];
        boolean anyConversion = false;
        for (int i = 0; i < oldArgs.length; i++) {
            if (assignable(oldArgs[i], newArgs[i])) continue;
            Conversion c = argConversion(oldArgs[i], newArgs[i], typeArgs.get(i));
            if (c == null) return null;
            argConversions[i] = c;
            anyConversion = true;
        }
        List<Conversion> argList = anyConversion ? Arrays.asList(argConversions) : null;
        if (constructor) return anyConversion ? new Adaptation(newDesc, null, false, argList, null) : new Adaptation(newDesc, null, false);
        Type expected = Type.getReturnType(oldDesc);
        Type actual = Type.getReturnType(newDesc);
        if (expected.getSort() == Type.VOID) {
            return new Adaptation(newDesc, null, actual.getSort() != Type.VOID, argList, null);
        }
        if (isPrimitive(expected) || isPrimitive(actual)) {
            return expected.equals(actual) ? new Adaptation(newDesc, null, false, argList, null) : null;
        }
        if (expected.equals(actual)) return new Adaptation(newDesc, null, false, argList, null);
        boolean expectedIsObject = expected.getDescriptor().equals("Ljava/lang/Object;");
        if (!expectedIsObject && assignable(actual, expected)) return new Adaptation(newDesc, null, false, argList, null);
        if (returnsTypeVariable(signature) && assignable(expected, actual)) {
            String cast = expected.getSort() == Type.ARRAY ? expected.getDescriptor() : expected.getInternalName();
            return new Adaptation(newDesc, cast, false, argList, null);
        }
        if (actual.getSort() == Type.OBJECT && expected.getSort() == Type.OBJECT) {
            Conversion back = conversions.find(actual.getInternalName(), expected.getInternalName());
            if (back != null) {
                return new Adaptation(newDesc, back.anyTarget() ? expected.getInternalName() : null, false,
                        argList != null ? argList : Arrays.asList(new Conversion[oldArgs.length]), back);
            }
        }
        return null;
    }

    /** Two different single-slot parameters that changed places. */
    private static boolean isSwap(Type[] oldArgs, Type[] newArgs) {
        return oldArgs.length == 2 && !oldArgs[0].equals(oldArgs[1])
                && oldArgs[0].equals(newArgs[1]) && oldArgs[1].equals(newArgs[0])
                && oldArgs[0].getSize() == 1 && oldArgs[1].getSize() == 1;
    }

    /**
     * A conversion for one argument: old type {@code from}, new parameter {@code to}. For a generic parameter
     * ({@code Holder<MobEffect>}) the value type must match its type argument; a type variable accepts any.
     */
    private Conversion argConversion(Type from, Type to, String typeArgument) {
        if (from.getSort() != Type.OBJECT || to.getSort() != Type.OBJECT) return null;
        Conversion c = conversions.find(from.getInternalName(), to.getInternalName());
        if (c == null || c.anyTarget()) return null;
        if (typeArgument != null && !typeArgument.equals("T")
                && !ClassIndex.isAssignable(from.getInternalName(), typeArgument, first, fallback)) return null;
        return c;
    }

    /**
     * For each parameter of a generic method signature, the internal name of its first type argument
     * ({@code Holder<MobEffect>} gives {@code net/minecraft/world/effect/MobEffect}), {@code "T"} for a type variable,
     * or {@code null}.
     */
    static List<String> paramTypeArguments(String signature, int count) {
        List<String> out = new ArrayList<>(java.util.Collections.nCopies(count, null));
        if (signature == null || signature.isEmpty()) return out;
        int[] param = {-1};
        new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM9) {
            int depth;
            boolean inParam;
            boolean captured;

            @Override
            public SignatureVisitor visitParameterType() {
                param[0]++;
                inParam = true;
                depth = 0;
                captured = false;
                return this;
            }

            @Override
            public SignatureVisitor visitReturnType() {
                inParam = false;
                return this;
            }

            @Override
            public void visitClassType(String name) {
                if (inParam) depth++;
                if (inParam && depth == 2 && !captured && param[0] < count) {
                    out.set(param[0], name);
                    captured = true;
                }
            }

            @Override
            public void visitTypeVariable(String name) {
                if (inParam && depth == 1 && !captured && param[0] < count) {
                    out.set(param[0], "T");
                    captured = true;
                }
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return this;
            }

            @Override
            public void visitEnd() {
                if (inParam) depth--;
            }
        });
        return out;
    }

    /** The adaptation for a field access whose type changed, or {@code null}. */
    public FieldAdaptation findField(String owner, String name, String desc, boolean isRead) {
        Type oldType = Type.getType(desc);
        if (oldType.getSort() != Type.OBJECT) return null;
        for (ClassIndex.Info info : ClassIndex.hierarchy(owner, first, fallback)) {
            if (info.fields().contains(name + ":" + desc)) return null;
            for (String f : info.fields()) {
                if (!f.startsWith(name + ":")) continue;
                Type newType = Type.getType(f.substring(name.length() + 1));
                if (newType.getSort() != Type.OBJECT) return null;
                // Narrowed type (InteractionResult.SUCCESS is an InteractionResult.Success now): reading needs no conversion.
                if (isRead && ClassIndex.isAssignable(newType.getInternalName(), oldType.getInternalName(), first, fallback)) {
                    return new FieldAdaptation(newType.getDescriptor(), null, null);
                }
                Conversion c = isRead ? conversions.find(newType.getInternalName(), oldType.getInternalName())
                        : conversions.find(oldType.getInternalName(), newType.getInternalName());
                if (c == null || (!isRead && c.anyTarget())) return null;
                String cast = c.anyTarget() ? oldType.getInternalName() : null;
                return new FieldAdaptation(newType.getDescriptor(), c, cast);
            }
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

    private record Trampoline(String name, int opcode, String owner, String method, String oldDesc, boolean itf, Adaptation adaptation) {}

    /** A class visitor (run after renaming) that rewrites adaptable calls and field accesses. */
    public ClassVisitor visitor(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            private String className;
            private boolean isInterface;
            private final List<Trampoline> trampolines = new ArrayList<>();
            private final Map<String, Trampoline> byCall = new LinkedHashMap<>();

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                className = name;
                isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor target = super.visitMethod(access, name, descriptor, signature, exceptions);
                // Buffer the method: rewriting a constructor call also removes its NEW/DUP.
                return new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions) {
                    @Override
                    public void visitEnd() {
                        rewrite(this);
                        accept(target);
                    }
                };
            }

            private void rewrite(MethodNode method) {
                Map<String, java.util.Deque<TypeInsnNode>> pendingNew = new java.util.HashMap<>();
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof TypeInsnNode t && t.getOpcode() == Opcodes.NEW) {
                        pendingNew.computeIfAbsent(t.desc, k -> new java.util.ArrayDeque<>()).push(t);
                    } else if (insn instanceof MethodInsnNode call) {
                        TypeInsnNode created = null;
                        if (call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("<init>")) {
                            var news = pendingNew.get(call.owner);
                            created = news != null ? news.poll() : null;
                        }
                        rewriteCall(method, call, created);
                    } else if (insn instanceof FieldInsnNode field) {
                        rewriteField(method, field);
                    }
                }
            }

            private void rewriteCall(MethodNode method, MethodInsnNode call, TypeInsnNode created) {
                int opcode = call.getOpcode();
                boolean constructor = call.name.equals("<init>");
                if (call.owner.startsWith("[") || (opcode == Opcodes.INVOKESPECIAL && !constructor)) return;
                if (exists(call.owner, call.name, call.desc)) return;
                Adaptation a = find(call.owner, call.name, call.desc, opcode == Opcodes.INVOKESTATIC);
                if (a == null) return;
                InsnList list = method.instructions;
                if (!a.needsTrampoline()) {
                    MethodInsnNode replacement = new MethodInsnNode(opcode, call.owner, call.name, a.newDesc(), call.itf);
                    list.set(call, replacement);
                    if (a.swap()) list.insertBefore(replacement, new InsnNode(Opcodes.SWAP));
                    if (a.castTo() != null) list.insert(replacement, new TypeInsnNode(Opcodes.CHECKCAST, a.castTo()));
                    if (a.pop()) list.insert(replacement, new InsnNode(Type.getReturnType(a.newDesc()).getSize() == 2 ? Opcodes.POP2 : Opcodes.POP));
                    return;
                }
                if (constructor) {
                    // new X(args) becomes X rose$adapt$n(args); only when the NEW/DUP pair is plain (no frames between).
                    if (created == null || created.getNext() == null || created.getNext().getOpcode() != Opcodes.DUP
                            || hasFrameBetween(created, call)) return;
                    list.remove(created.getNext());
                    list.remove(created);
                }
                Trampoline t = trampoline(opcode, call.owner, call.name, call.desc, call.itf, a);
                list.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, className, t.name(), trampolineDesc(t), isInterface));
            }

            private void rewriteField(MethodNode method, FieldInsnNode field) {
                int opcode = field.getOpcode();
                boolean isRead = opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD;
                FieldAdaptation f = field.owner.startsWith("[") ? null : findField(field.owner, field.name, field.desc, isRead);
                if (f == null) return;
                InsnList list = method.instructions;
                FieldInsnNode replacement = new FieldInsnNode(opcode, field.owner, field.name, f.newDesc());
                list.set(field, replacement);
                if (f.conversion() == null) return;
                InsnList conversion = new InsnList();
                conversion.add(new MethodInsnNode(Opcodes.INVOKESTATIC, f.conversion().helperOwner(), f.conversion().helperName(),
                        f.conversion().helperDescriptor(), false));
                if (f.castTo() != null) conversion.add(new TypeInsnNode(Opcodes.CHECKCAST, f.castTo()));
                if (isRead) list.insert(replacement, conversion);
                else list.insertBefore(replacement, conversion);
            }

            private Trampoline trampoline(int opcode, String owner, String method, String desc, boolean itf, Adaptation a) {
                String key = opcode + " " + owner + "." + method + desc;
                return byCall.computeIfAbsent(key, k -> {
                    Trampoline t = new Trampoline("rose$adapt$" + trampolines.size(), opcode, owner, method, desc, itf, a);
                    trampolines.add(t);
                    return t;
                });
            }

            private String trampolineDesc(Trampoline t) {
                if (t.method().equals("<init>")) return t.oldDesc().substring(0, t.oldDesc().indexOf(')') + 1) + "L" + t.owner() + ";";
                if (t.opcode() == Opcodes.INVOKESTATIC) return t.oldDesc();
                return "(L" + t.owner() + ";" + t.oldDesc().substring(1);
            }

            @Override
            public void visitEnd() {
                for (Trampoline t : trampolines) writeTrampoline(t);
                super.visitEnd();
            }

            private void writeTrampoline(Trampoline t) {
                int access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC;
                MethodVisitor mv = super.visitMethod(access, t.name(), trampolineDesc(t), null, null);
                mv.visitCode();
                boolean constructor = t.method().equals("<init>");
                int slot = 0;
                if (constructor) {
                    mv.visitTypeInsn(Opcodes.NEW, t.owner());
                    mv.visitInsn(Opcodes.DUP);
                } else if (t.opcode() != Opcodes.INVOKESTATIC) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    slot = 1;
                }
                Type[] args = Type.getArgumentTypes(t.oldDesc());
                Adaptation a = t.adaptation();
                for (int i = 0; i < args.length; i++) {
                    mv.visitVarInsn(args[i].getOpcode(Opcodes.ILOAD), slot);
                    slot += args[i].getSize();
                    Conversion c = a.argConversions() != null ? a.argConversions().get(i) : null;
                    if (c != null) convert(mv, c, null);
                }
                mv.visitMethodInsn(t.opcode(), t.owner(), t.method(), a.newDesc(), t.itf());
                if (constructor) {
                    mv.visitInsn(Opcodes.ARETURN);
                } else {
                    Type oldReturn = Type.getReturnType(t.oldDesc());
                    if (a.returnConversion() != null) convert(mv, a.returnConversion(), a.castTo());
                    else if (a.castTo() != null) mv.visitTypeInsn(Opcodes.CHECKCAST, a.castTo());
                    if (a.pop()) mv.visitInsn(Type.getReturnType(a.newDesc()).getSize() == 2 ? Opcodes.POP2 : Opcodes.POP);
                    mv.visitInsn(oldReturn.getOpcode(Opcodes.IRETURN));
                }
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        };
    }

    private static boolean hasFrameBetween(AbstractInsnNode from, AbstractInsnNode to) {
        for (AbstractInsnNode i = from; i != null && i != to; i = i.getNext()) {
            if (i instanceof FrameNode) return true;
        }
        return false;
    }

    private static void convert(MethodVisitor mv, Conversion c, String castTo) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, c.helperOwner(), c.helperName(), c.helperDescriptor(), false);
        if (castTo != null) mv.visitTypeInsn(Opcodes.CHECKCAST, castTo);
    }
}

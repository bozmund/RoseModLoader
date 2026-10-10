package rose.translate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import rose.rosetta.MixinRetargetRules;
import rose.rosetta.MixinRetargetRules.Retarget;

/**
 * Moves old Mixins onto the 26.3 methods their targets became ({@code mixin-retargets.tsv}), after Rosetta renamed
 * them. Selectors are rewritten in the refmap (or the annotation, without one). An {@code @Inject} handler into a
 * retargeted method keeps its body: a new handler with the new target's arguments calls it, passing each old argument
 * from the new argument of the same type (or a subtype), and nothing for an old argument it never reads. Locals the
 * old handler captured ({@code locals = CAPTURE_*}, which depends on the old method's exact locals) become
 * MixinExtras {@code @Local} parameters, found by type. A {@code @Shadow} of a retargeted method is renamed when only
 * its name changed, and dropped when nothing calls it.
 */
public final class MixinRebaser {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String SHADOW = "Lorg/spongepowered/asm/mixin/Shadow;";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String LOCAL = "Lcom/llamalad7/mixinextras/sugar/Local;";
    private static final String CALLBACK = "org/spongepowered/asm/mixin/injection/callback/CallbackInfo";
    private static final String CALLBACK_RETURNABLE = "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable";

    private final MixinRetargetRules rules;
    private final ClassIndex game;
    /** Per Mixin class (internal name): refmap key to the retarget applied to its value. */
    private final Map<String, Map<String, Retarget>> retargeted = new HashMap<>();
    private final List<String> report = new ArrayList<>();

    public MixinRebaser(MixinRetargetRules rules, ClassIndex game) {
        this.rules = rules;
        this.game = game;
    }

    /** What was rebased, or couldn't be. */
    public List<String> report() {
        return List.copyOf(report);
    }

    /** A translated refmap with its selectors retargeted. Call for every refmap before {@link #rebase}. */
    public String retargetRefmap(String refmapJson) {
        JsonObject root = JsonParser.parseString(refmapJson).getAsJsonObject();
        if (root.get("mappings") instanceof JsonObject mappings) retarget(mappings, true);
        if (root.get("data") instanceof JsonObject data) {
            for (var environment : data.entrySet()) {
                if (environment.getValue() instanceof JsonObject mappings) retarget(mappings, false);
            }
        }
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root);
    }

    private void retarget(JsonObject mappings, boolean record) {
        for (var mixin : mappings.entrySet()) {
            if (!(mixin.getValue() instanceof JsonObject entries)) continue;
            for (var entry : entries.entrySet()) {
                if (!(entry.getValue() instanceof JsonPrimitive value)) continue;
                Retarget r = rules.find(value.getAsString());
                if (r != null) {
                    entry.setValue(new JsonPrimitive(r.newSelector()));
                    if (record) retargeted.computeIfAbsent(mixin.getKey(), k -> new HashMap<>()).put(entry.getKey(), r);
                }
            }
        }
    }

    /** A translated class, with its Mixin selectors and handlers moved to retargeted methods. Others are unchanged. */
    public byte[] rebase(byte[] bytes) {
        if (rules.isEmpty()) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        List<String> targets = mixinTargets(node);
        if (targets.isEmpty()) return bytes;
        boolean changed = false;
        for (MethodNode method : new ArrayList<>(node.methods)) {
            if (has(method, SHADOW)) changed |= shadow(node, method, targets);
            AnnotationNode inject = annotation(method, INJECT);
            if (inject != null) changed |= inject(node, method, inject, targets);
        }
        if (!changed) return bytes;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private boolean shadow(ClassNode node, MethodNode method, List<String> targets) {
        for (String target : targets) {
            Retarget r = rules.find(target, method.name, method.desc);
            if (r == null) continue;
            if (r.newDescriptor().equals(method.desc)) {
                renameCalls(node, method.name, method.desc, r.newName());
                method.name = r.newName();
                report.add(node.name + ": @Shadow " + r.oldSelector() + " -> " + r.newSelector());
                return true;
            }
            if (!calls(node, method.name, method.desc)) {
                node.methods.remove(method);
                report.add(node.name + ": dropped unused @Shadow " + r.oldSelector() + " (now " + r.newSelector() + ")");
                return true;
            }
            report.add(node.name + ": can't rebase @Shadow " + r.oldSelector() + ": it is called and its signature changed");
        }
        return false;
    }

    private boolean inject(ClassNode node, MethodNode handler, AnnotationNode inject, List<String> targets) {
        Retarget method = null;
        List<Object> values = inject.values;
        for (int i = 0; values != null && i < values.size(); i += 2) {
            if (!"method".equals(values.get(i))) continue;
            @SuppressWarnings("unchecked") List<String> selectors = (List<String>) values.get(i + 1);
            for (int s = 0; s < selectors.size(); s++) {
                Retarget r = retargeted.getOrDefault(node.name, Map.of()).get(selectors.get(s));
                if (r == null) {
                    r = selectorRetarget(selectors.get(s), targets);
                    if (r != null) selectors.set(s, r.newSelector()); // no refmap: the annotation names the method
                }
                if (r != null) method = r;
            }
        }
        if (method == null) return false;
        Type[] handlerArgs = Type.getArgumentTypes(handler.desc);
        Type[] oldArgs = Type.getArgumentTypes(method.oldDescriptor());
        Type[] newArgs = Type.getArgumentTypes(method.newDescriptor());
        int callback = callbackIndex(handlerArgs);
        boolean capture = removeLocalCapture(inject);
        boolean withArgs = callback == oldArgs.length && callback > 0;
        if (callback < 0 || (callback != 0 && !withArgs)) {
            report.add(node.name + "." + handler.name + ": can't rebase onto " + method.newSelector() + ": unexpected handler arguments " + handler.desc);
            return true; // the selector moved; Mixin reports the handler
        }
        boolean handlerStatic = (handler.access & Opcodes.ACC_STATIC) != 0;
        boolean targetStatic = isStatic(method.newSelector());
        if (!withArgs && !capture && handlerStatic == targetStatic) {
            report.add(node.name + "." + handler.name + ": -> " + method.newSelector());
            return true;
        }
        if (targetStatic && !handlerStatic) {
            report.add(node.name + "." + handler.name + ": can't rebase onto static " + method.newSelector() + ": the handler needs its instance");
            return true;
        }
        int[] source = new int[callback]; // per old target argument: its new argument, or -1 (pass a default)
        boolean[] taken = new boolean[newArgs.length];
        for (int i = 0; i < callback; i++) {
            source[i] = matchArgument(oldArgs[i], newArgs, taken);
            if (source[i] >= 0) taken[source[i]] = true;
            else if (reads(handler, slotOf(handler, handlerArgs, i))) {
                report.add(node.name + "." + handler.name + ": can't rebase onto " + method.newSelector() + ": no argument for its "
                        + oldArgs[i].getClassName() + ", which it reads");
                return true;
            }
        }
        Type[] captured = java.util.Arrays.copyOfRange(handlerArgs, callback + 1, handlerArgs.length);
        node.methods.add(adapter(node, handler, inject, withArgs ? newArgs : new Type[0], handlerArgs[callback], captured, source, capture, targetStatic));
        report.add(node.name + "." + handler.name + ": -> " + method.newSelector() + (capture ? " (captured locals as @Local)" : ""));
        return true;
    }

    /** A handler with the new target's arguments that calls the old one; the injector annotation moves to it. */
    private MethodNode adapter(ClassNode node, MethodNode handler, AnnotationNode inject, Type[] newArgs, Type callbackType,
                               Type[] captured, int[] source, boolean capture, boolean isStatic) {
        boolean handlerStatic = (handler.access & Opcodes.ACC_STATIC) != 0;
        List<Type> params = new ArrayList<>(List.of(newArgs));
        params.add(callbackType);
        params.addAll(List.of(captured));
        String desc = Type.getMethodDescriptor(Type.VOID_TYPE, params.toArray(Type[]::new));
        MethodNode adapter = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PRIVATE | (isStatic ? Opcodes.ACC_STATIC : 0),
                handler.name + "$rose", desc, null, null);
        boolean visible = handler.visibleAnnotations != null && handler.visibleAnnotations.remove(inject);
        if (!visible) handler.invisibleAnnotations.remove(inject);
        if (visible) adapter.visibleAnnotations = new ArrayList<>(List.of(inject));
        else adapter.invisibleAnnotations = new ArrayList<>(List.of(inject));
        if (capture) {
            adapter.invisibleParameterAnnotations = newArrayOfLists(params.size());
            for (int p = params.size() - captured.length; p < params.size(); p++) {
                adapter.invisibleParameterAnnotations[p] = new ArrayList<>(List.of(new AnnotationNode(LOCAL)));
            }
        }
        // the old handler becomes a plain private method of the Mixin
        handler.access = (handler.access & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PRIVATE;

        int[] slots = new int[params.size()];
        int slot = isStatic ? 0 : 1;
        for (int p = 0; p < params.size(); p++) {
            slots[p] = slot;
            slot += params.get(p).getSize();
        }
        InsnList code = adapter.instructions;
        if (!handlerStatic) code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        Type[] oldArgs = Type.getArgumentTypes(handler.desc);
        for (int i = 0; i < source.length; i++) {
            if (source[i] >= 0) code.add(new VarInsnNode(newArgs[source[i]].getOpcode(Opcodes.ILOAD), slots[source[i]]));
            else code.add(defaultValue(oldArgs[i]));
        }
        code.add(new VarInsnNode(Opcodes.ALOAD, slots[newArgs.length]));
        for (int c = 0; c < captured.length; c++) {
            code.add(new VarInsnNode(captured[c].getOpcode(Opcodes.ILOAD), slots[newArgs.length + 1 + c]));
        }
        int opcode = handlerStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL;
        code.add(new MethodInsnNode(opcode, node.name, handler.name, handler.desc, false));
        code.add(new InsnNode(Opcodes.RETURN));
        return adapter;
    }

    /** Whether the (26.3) method a selector names is static. */
    private boolean isStatic(String selector) {
        String owner = selector.substring(1, selector.indexOf(';'));
        ClassIndex.Info info = game.get(owner);
        return info != null && info.staticMethods().contains(selector.substring(selector.indexOf(';') + 1));
    }

    /** The new argument an old one is: the same type, else a subtype; each used once. -1 when none. */
    private int matchArgument(Type old, Type[] newArgs, boolean[] taken) {
        int found = -1;
        for (int j = 0; j < newArgs.length; j++) {
            if (!taken[j] && newArgs[j].equals(old)) {
                if (found >= 0) return -1; // ambiguous
                found = j;
            }
        }
        if (found >= 0 || old.getSort() != Type.OBJECT) return found;
        for (int j = 0; j < newArgs.length; j++) {
            if (!taken[j] && newArgs[j].getSort() == Type.OBJECT
                    && ClassIndex.isAssignable(newArgs[j].getInternalName(), old.getInternalName(), game, null)) {
                if (found >= 0) return -1;
                found = j;
            }
        }
        return found;
    }

    private static AbstractInsnNode defaultValue(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> new InsnNode(Opcodes.ICONST_0);
            case Type.LONG -> new InsnNode(Opcodes.LCONST_0);
            case Type.FLOAT -> new InsnNode(Opcodes.FCONST_0);
            case Type.DOUBLE -> new InsnNode(Opcodes.DCONST_0);
            default -> new InsnNode(Opcodes.ACONST_NULL);
        };
    }

    /** A selector written out in the annotation ({@code Lowner;name(desc)} or {@code name(desc)}), retargeted. */
    private Retarget selectorRetarget(String selector, List<String> targets) {
        if (selector.startsWith("L")) return rules.find(selector);
        int paren = selector.indexOf('(');
        if (paren < 0) return null;
        for (String target : targets) {
            Retarget r = rules.find(target, selector.substring(0, paren), selector.substring(paren));
            if (r != null) return r;
        }
        return null;
    }

    /** Removes {@code locals = ...} and says whether it captured (anything but NO_CAPTURE). */
    private static boolean removeLocalCapture(AnnotationNode inject) {
        for (int i = 0; inject.values != null && i < inject.values.size(); i += 2) {
            if ("locals".equals(inject.values.get(i))) {
                String[] value = (String[]) inject.values.get(i + 1);
                if ("NO_CAPTURE".equals(value[1])) return false;
                inject.values.remove(i + 1);
                inject.values.remove(i);
                return true;
            }
        }
        return false;
    }

    private static int callbackIndex(Type[] args) {
        for (int i = 0; i < args.length; i++) {
            String name = args[i].getSort() == Type.OBJECT ? args[i].getInternalName() : "";
            if (name.equals(CALLBACK) || name.equals(CALLBACK_RETURNABLE)) return i;
        }
        return -1;
    }

    private static int slotOf(MethodNode method, Type[] args, int index) {
        int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        for (int i = 0; i < index; i++) slot += args[i].getSize();
        return slot;
    }

    private static boolean reads(MethodNode method, int slot) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof VarInsnNode v && v.var == slot) return true;
            if (insn instanceof IincInsnNode inc && inc.var == slot) return true;
        }
        return false;
    }

    private static boolean calls(ClassNode node, String name, String desc) {
        for (MethodNode m : node.methods) {
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(node.name) && call.name.equals(name) && call.desc.equals(desc)) return true;
            }
        }
        return false;
    }

    private static void renameCalls(ClassNode node, String name, String desc, String newName) {
        for (MethodNode m : node.methods) {
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(node.name) && call.name.equals(name) && call.desc.equals(desc)) call.name = newName;
            }
        }
    }

    /** The classes a Mixin targets (internal names); empty for other classes. */
    @SuppressWarnings("unchecked")
    private static List<String> mixinTargets(ClassNode node) {
        AnnotationNode mixin = null;
        for (List<AnnotationNode> list : java.util.Arrays.asList(node.visibleAnnotations, node.invisibleAnnotations)) {
            if (list == null) continue;
            for (AnnotationNode a : list) if (a.desc.equals(MIXIN)) mixin = a;
        }
        List<String> out = new ArrayList<>();
        if (mixin == null || mixin.values == null) return out;
        for (int i = 0; i < mixin.values.size(); i += 2) {
            Object value = mixin.values.get(i + 1);
            if ("value".equals(mixin.values.get(i))) ((List<Type>) value).forEach(t -> out.add(t.getInternalName()));
            if ("targets".equals(mixin.values.get(i))) ((List<String>) value).forEach(t -> out.add(t.replace('.', '/')));
        }
        return out;
    }

    private static boolean has(MethodNode method, String desc) {
        return annotation(method, desc) != null;
    }

    private static AnnotationNode annotation(MethodNode method, String desc) {
        for (List<AnnotationNode> list : java.util.Arrays.asList(method.visibleAnnotations, method.invisibleAnnotations)) {
            if (list == null) continue;
            for (AnnotationNode a : list) if (a.desc.equals(desc)) return a;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<AnnotationNode>[] newArrayOfLists(int size) {
        return (List<AnnotationNode>[]) new List<?>[size];
    }
}

package rose.analyzer;

import rose.translate.ClassIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Sorts a mod's classes by when they run, so the report can tell real blockers from the rest:
 * <ul>
 *   <li><b>data generation</b>: build-time generators (recipes, loot tables, models written to JSON). A class is one
 *       if it extends/implements a data-generation type or listens to {@code GatherDataEvent};</li>
 *   <li><b>integration</b>: code that only runs when another mod is installed (JEI, EMI, CraftTweaker...): classes in
 *       an {@code integration}/{@code compat} package;</li>
 *   <li><b>runtime</b>: everything else.</li>
 * </ul>
 * Nested classes follow their outer class, and a class used only by classes of one context joins it.
 * Mixins and {@code @Mod} classes are always runtime.
 */
record ClassContexts(Set<String> dataGeneration, Set<String> integration) {
    private static final List<String> DATA_PREFIXES = List.of(
            "net/minecraft/data/", "net/minecraftforge/common/data/", "net/minecraftforge/data/",
            "net/minecraftforge/client/model/generators/");
    private static final List<String> INTEGRATION_SEGMENTS = List.of("integration", "compat", "compatibility", "integrations");

    boolean runtime(String className) {
        return !dataGeneration.contains(className) && !integration.contains(className);
    }

    static ClassContexts find(List<ClassNode> nodes, ClassIndex mod, ClassIndex vanillaOld) {
        Map<String, Set<String>> usedBy = new HashMap<>();
        Set<String> names = new HashSet<>();
        nodes.forEach(n -> names.add(n.name));
        Set<String> alwaysRuntime = new HashSet<>();
        Set<String> data = new HashSet<>();
        Set<String> integration = new HashSet<>();

        for (ClassNode node : nodes) {
            if (isEntryPointOrMixin(node)) alwaysRuntime.add(node.name);
            Set<String> refs = referencedClasses(node);
            for (String ref : refs) {
                if (names.contains(ref) && !ref.equals(node.name)) usedBy.computeIfAbsent(ref, k -> new HashSet<>()).add(node.name);
            }
            boolean dataType = ClassIndex.hierarchy(node.name, mod, vanillaOld).stream()
                    .anyMatch(i -> Analyzer.startsWithAny(i.name(), DATA_PREFIXES))
                    || node.interfaces.stream().anyMatch(i -> Analyzer.startsWithAny(i, DATA_PREFIXES))
                    || (node.superName != null && Analyzer.startsWithAny(node.superName, DATA_PREFIXES));
            if (dataType || refs.contains("net/minecraftforge/data/event/GatherDataEvent")) data.add(node.name);
            if (inIntegrationPackage(node.name)) integration.add(node.name);
        }

        spread(nodes, data, usedBy, alwaysRuntime);
        spread(nodes, integration, usedBy, alwaysRuntime);
        data.removeAll(alwaysRuntime);
        integration.removeAll(alwaysRuntime);
        integration.removeAll(data);
        return new ClassContexts(data, integration);
    }

    /** Adds nested classes and classes used only by members of {@code set}, until nothing changes. */
    private static void spread(List<ClassNode> nodes, Set<String> set, Map<String, Set<String>> usedBy, Set<String> excluded) {
        Predicate<String> member = set::contains;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ClassNode node : nodes) {
                if (set.contains(node.name) || excluded.contains(node.name)) continue;
                int dollar = node.name.indexOf('$');
                boolean nested = dollar > 0 && member.test(node.name.substring(0, dollar));
                Set<String> users = usedBy.getOrDefault(node.name, Set.of());
                boolean onlyUsedBySet = !users.isEmpty() && set.containsAll(users);
                if (nested || onlyUsedBySet) {
                    set.add(node.name);
                    changed = true;
                }
            }
        }
    }

    private static boolean inIntegrationPackage(String className) {
        String[] segments = className.toLowerCase(Locale.ROOT).split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            if (INTEGRATION_SEGMENTS.contains(segments[i])) return true;
        }
        return false;
    }

    private static boolean isEntryPointOrMixin(ClassNode node) {
        List<AnnotationNode> annotations = new ArrayList<>();
        if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
        if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);
        return annotations.stream().anyMatch(a -> a.desc.equals("Lnet/minecraftforge/fml/common/Mod;")
                || a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"));
    }

    private static Set<String> referencedClasses(ClassNode node) {
        Set<String> out = new HashSet<>();
        if (node.superName != null) out.add(node.superName);
        out.addAll(node.interfaces);
        for (FieldNode f : node.fields) addType(out, Type.getType(f.desc));
        for (MethodNode m : node.methods) {
            addType(out, Type.getMethodType(m.desc));
            for (AbstractInsnNode insn = m.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                switch (insn) {
                    case MethodInsnNode mi -> {
                        out.add(mi.owner);
                        addType(out, Type.getMethodType(mi.desc));
                    }
                    case FieldInsnNode fi -> out.add(fi.owner);
                    case TypeInsnNode ti -> out.add(ti.desc);
                    default -> { }
                }
            }
        }
        return out;
    }

    private static void addType(Set<String> out, Type type) {
        switch (type.getSort()) {
            case Type.ARRAY -> addType(out, type.getElementType());
            case Type.OBJECT -> out.add(type.getInternalName());
            case Type.METHOD -> {
                addType(out, type.getReturnType());
                for (Type arg : type.getArgumentTypes()) addType(out, arg);
            }
            default -> { }
        }
    }
}

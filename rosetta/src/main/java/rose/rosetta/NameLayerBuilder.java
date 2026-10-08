package rose.rosetta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import rose.rosetta.NameLayer.ClassEntry;
import rose.rosetta.NameLayer.How;
import rose.rosetta.NameLayer.MemberEntry;

/**
 * Builds the Forge 1.20.1 to 26.3 name layer by joining:
 * <ol>
 *   <li>MCPConfig 1.20.1 (official to SRG member names, which Forge mods use at runtime),</li>
 *   <li>Mojang 1.20.1 mappings (official to the class names Forge mods use, and readable member names),</li>
 *   <li>Fabric intermediary 1.20.1 and 1.21.11 (a name that stays the same for each class/member across versions),</li>
 *   <li>Mojang 1.21.11 mappings (the last obfuscated version; 26.x keeps Mojang's names),</li>
 *   <li>Rose's reviewed rename rules for 1.21.11 to 26.3, and the class list of the 26.3 jar.</li>
 * </ol>
 */
public final class NameLayerBuilder {
    public record Inputs(Path mcpConfig, Path intermediaryOld, Path mojangOld,
                         Path intermediaryNew, Path mojangNew, Path targetJar, List<Path> ruleFiles) {

        /** The standard corpus layout for Forge 1.20.1. */
        public static Inputs forge1201(Path corpus, Path rulesDir) {
            return new Inputs(
                    corpus.resolve("mappings/mcp_config-1.20.1.zip"),
                    corpus.resolve("mappings/intermediary-1.20.1-v2.jar"),
                    corpus.resolve("minecraft/1.20.1/client-mappings.txt"),
                    corpus.resolve("mappings/intermediary-1.21.11-v2.jar"),
                    corpus.resolve("minecraft/1.21.11/client-mappings.txt"),
                    corpus.resolve("minecraft/26.3/client.jar"),
                    List.of(rulesDir.resolve("class-renames-1.21.11-to-26.3.tsv")));
        }
    }

    public static NameLayer buildForge1201(Inputs in) throws IOException {
        Mappings srg = Mappings.srg(in.mcpConfig());
        Mappings intOld = Mappings.intermediary(in.intermediaryOld());
        Mappings mojOld = Mappings.mojang(in.mojangOld());
        Mappings intNew = Mappings.intermediary(in.intermediaryNew());
        Mappings mojNew = Mappings.mojang(in.mojangNew());
        Set<String> targetClasses = classesIn(in.targetJar());

        Map<String, String> renames = new HashMap<>();
        for (Path rules : in.ruleFiles()) renames.putAll(RenameRules.read(rules).renames());

        // Intermediary name -> Mojang name in 1.21.11, for classes, methods and fields.
        Map<String, String> classByInt = join(intNew.classes, mojNew.classes);
        Map<String, String> methodByInt = join(intNew.methods, mojNew.methods);
        Map<String, String> fieldByInt = join(intNew.fields, mojNew.fields);

        Set<String> newMojangClasses = new HashSet<>(mojNew.classes.values());
        Map<String, List<String>> appearedBySimpleName = appearedClassesBySimpleName(targetClasses, newMojangClasses);

        Map<String, ClassEntry> classes = new HashMap<>();
        for (var e : mojOld.classes.entrySet()) {
            String oldName = e.getValue();
            String intName = intOld.classes.get(e.getKey());
            String in1211 = intName != null ? classByInt.get(intName) : null;
            classes.put(oldName, classEntry(oldName, in1211, renames, targetClasses, appearedBySimpleName));
        }

        // Old (Mojang 1.20.1) class name -> 26.3 class name, for translating descriptors.
        java.util.function.UnaryOperator<String> toNew = name -> {
            ClassEntry c = classes.get(name);
            return c != null && c.how() != How.GONE ? c.newName() : name;
        };
        Set<String> targetMembers = membersIn(in.targetJar());

        Map<String, MemberEntry> methods = new HashMap<>();
        for (var e : srg.methods.entrySet()) {
            String srgName = e.getValue();
            if (!srgName.startsWith("m_")) continue; // only obfuscated methods have SRG names
            String readable = mojOld.methods.getOrDefault(e.getKey(), srgName);
            String intName = intOld.methods.get(e.getKey());
            String newName = intName != null ? methodByInt.get(intName) : null;
            MemberEntry entry = member(srgName, readable, newName);
            if (!entry.exists() && sameSignatureExists(e.getKey(), readable, mojOld.classes, toNew, targetMembers, true)) {
                entry = new MemberEntry(srgName, readable, readable, How.NAME_MATCH);
            }
            putPreferResolved(methods, entry);
        }

        Map<String, MemberEntry> fields = new HashMap<>();
        for (var e : srg.fields.entrySet()) {
            String srgName = e.getValue();
            if (!srgName.startsWith("f_")) continue;
            String readable = mojOld.fields.getOrDefault(e.getKey(), srgName);
            String intName = intOld.fields.get(e.getKey());
            String newName = intName != null ? fieldByInt.get(intName) : null;
            MemberEntry entry = member(srgName, readable, newName);
            String fieldDesc = intOld.fieldDescs.get(e.getKey());
            if (!entry.exists() && fieldDesc != null
                    && sameSignatureExists(e.getKey() + ":" + fieldDesc, readable, mojOld.classes, toNew, targetMembers, false)) {
                entry = new MemberEntry(srgName, readable, readable, How.NAME_MATCH);
            }
            putPreferResolved(fields, entry);
        }

        return new NameLayer("forge-1.20.1", classes, methods, fields);
    }

    private static ClassEntry classEntry(String oldName, String in1211, Map<String, String> renames,
                                         Set<String> targetClasses, Map<String, List<String>> appearedBySimpleName) {
        if (in1211 == null) return new ClassEntry(oldName, "-", How.GONE);
        String ruled = renames.get(in1211);
        if (ruled != null) return new ClassEntry(oldName, ruled, How.RULE);
        if (targetClasses.contains(in1211)) {
            return new ClassEntry(oldName, in1211, in1211.equals(oldName) ? How.SAME : How.INTERMEDIARY);
        }
        // Not in 26.3 under its 1.21.11 name. If exactly one class with the same simple name appeared in 26.3,
        // it most likely moved packages.
        List<String> candidates = appearedBySimpleName.getOrDefault(simpleName(in1211), List.of());
        if (candidates.size() == 1) return new ClassEntry(oldName, candidates.getFirst(), How.HEURISTIC);
        return new ClassEntry(oldName, "-", How.GONE);
    }

    /**
     * Intermediary gives a method a new name when it moves between classes or Fabric's matcher loses it, even when
     * nothing about it changed. If 26.3 declares a member with the same readable name and an identical (translated)
     * signature in the corresponding class, it is the same member.
     *
     * @param officialKey {@code owner.name(desc)} for methods or {@code owner.name:desc} for fields, in obfuscated names
     */
    private static boolean sameSignatureExists(String officialKey, String readable, Map<String, String> officialToOld,
                                               java.util.function.UnaryOperator<String> oldToNew, Set<String> targetMembers,
                                               boolean method) {
        int dot = officialKey.indexOf('.');
        String owner = officialToOld.get(officialKey.substring(0, dot));
        if (owner == null) return false;
        int descStart = method ? officialKey.indexOf('(', dot) : officialKey.indexOf(':', dot) + 1;
        if (descStart <= 0) return false;
        String desc = Descriptors.map(officialKey.substring(descStart), name -> oldToNew.apply(officialToOld.getOrDefault(name, name)));
        String key = oldToNew.apply(owner) + "." + readable + (method ? desc : ":" + desc);
        return targetMembers.contains(key);
    }

    /** {@code owner.name(desc)} for every method and {@code owner.name:desc} for every field declared in a jar. */
    static Set<String> membersIn(Path jar) throws IOException {
        Set<String> out = new HashSet<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class") || entry.getName().startsWith("META-INF/")) continue;
                try (var stream = zip.getInputStream(entry)) {
                    new org.objectweb.asm.ClassReader(stream.readAllBytes()).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                        String owner;

                        @Override
                        public void visit(int v, int a, String name, String sig, String sup, String[] itf) {
                            owner = name;
                        }

                        @Override
                        public org.objectweb.asm.MethodVisitor visitMethod(int a, String name, String desc, String sig, String[] ex) {
                            out.add(owner + "." + name + desc);
                            return null;
                        }

                        @Override
                        public org.objectweb.asm.FieldVisitor visitField(int a, String name, String desc, String sig, Object v) {
                            out.add(owner + "." + name + ":" + desc);
                            return null;
                        }
                    }, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG);
                }
            }
        }
        return out;
    }

    /**
     * One SRG name covers a whole override family, so it appears once per class that declares the method. Some of
     * those classes (or their entries) didn't survive to 1.21.11 while the family did, so any resolved entry wins.
     */
    private static void putPreferResolved(Map<String, MemberEntry> map, MemberEntry entry) {
        MemberEntry existing = map.get(entry.oldName());
        boolean better = existing == null
                || (!existing.exists() && entry.exists())
                || (existing.how() == How.NAME_MATCH && (entry.how() == How.SAME || entry.how() == How.INTERMEDIARY));
        if (better) map.put(entry.oldName(), entry);
    }

    private static MemberEntry member(String srgName, String readable, String newName) {
        if (newName == null) return new MemberEntry(srgName, readable, "-", How.GONE);
        return new MemberEntry(srgName, readable, newName, newName.equals(readable) ? How.SAME : How.INTERMEDIARY);
    }

    /** For keys present in both maps: a's value -> b's value (e.g. intermediary name -> Mojang name). */
    private static Map<String, String> join(Map<String, String> a, Map<String, String> b) {
        Map<String, String> out = new HashMap<>();
        for (var e : a.entrySet()) {
            String other = b.get(e.getKey());
            if (other != null) out.putIfAbsent(e.getValue(), other);
        }
        return out;
    }

    /** 26.3 classes that did not exist in 1.21.11, grouped by simple name (inner classes use their full $ name). */
    private static Map<String, List<String>> appearedClassesBySimpleName(Set<String> target, Set<String> previous) {
        Map<String, List<String>> out = new HashMap<>();
        for (String name : target) {
            if (previous.contains(name)) continue;
            out.computeIfAbsent(simpleName(name), k -> new java.util.ArrayList<>()).add(name);
        }
        return out;
    }

    static String simpleName(String internalName) {
        return internalName.substring(internalName.lastIndexOf('/') + 1);
    }

    static Set<String> classesIn(Path jar) throws IOException {
        Set<String> out = new HashSet<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            zip.stream().map(e -> e.getName())
                    .filter(n -> n.endsWith(".class") && !n.startsWith("META-INF/"))
                    .forEach(n -> out.add(n.substring(0, n.length() - ".class".length())));
        }
        return out;
    }

    private NameLayerBuilder() {}
}

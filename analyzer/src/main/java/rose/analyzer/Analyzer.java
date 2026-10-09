package rose.analyzer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import rose.analyzer.Finding.Status;
import rose.rosetta.NameLayer;
import rose.rosetta.ConversionRules;
import rose.rosetta.RedirectRules;
import rose.translate.ClassIndex;
import rose.translate.CallAdapter;
import rose.translate.JarTranslator;
import rose.translate.RosettaRemapper;

/**
 * Static compatibility check of one old mod jar against Minecraft 26.3.
 *
 * <p>Every class, method and field the mod references is translated with Rosetta, then looked up (including
 * inherited members) in 26.3 and its libraries. What can't be resolved becomes a {@link Finding}: work for the era
 * bridge, the dialect, the Mixin rebaser, or a missing dependency. Pure renames resolve and produce no finding.
 */
public final class Analyzer {
    static final List<String> FORGE_PREFIXES = List.of("net/minecraftforge/", "cpw/mods/");
    static final List<String> PROVIDED_PREFIXES = List.of("org/spongepowered/", "com/llamalad7/mixinextras/");
    static final List<String> MINECRAFT_PREFIXES = List.of("net/minecraft/", "com/mojang/");

    private final NameLayer layer;
    private final RosettaRemapper remapper;
    private final RedirectRules redirects;
    private int redirected;
    private int adapted;
    private CallAdapter adapter;
    private ConversionRules conversions = ConversionRules.empty();
    private final ClassIndex target;
    private final ClassIndex vanillaOld;
    private final Map<String, Finding> findings = new LinkedHashMap<>();
    private final Map<String, Integer> forgeSurface = new HashMap<>();
    private ClassIndex modOriginal;
    private ClassIndex modTranslated;
    private ClassContexts contexts = new ClassContexts(Set.of(), Set.of());
    private int references;

    /**
     * @param target     26.3 client jar and its libraries
     * @param vanillaOld the source version's client with readable (Mojang) names, to tell Forge-added methods apart
     */
    public Analyzer(NameLayer layer, ClassIndex target, ClassIndex vanillaOld) {
        this(layer, RedirectRules.empty(), target, vanillaOld);
    }

    /** @param target must include the era bridge jar so redirect shims can be checked */
    public Analyzer(NameLayer layer, RedirectRules redirects, ClassIndex target, ClassIndex vanillaOld) {
        this.layer = layer;
        this.redirects = redirects;
        this.remapper = new RosettaRemapper(layer);
        this.target = target;
        this.vanillaOld = vanillaOld;
    }

    /** Value conversions the translator may insert (see CallAdapter); calls they fix aren't reported. */
    public Analyzer withConversions(ConversionRules conversions) {
        this.conversions = conversions;
        return this;
    }

    public Report analyze(Path modJar) throws IOException {
        modOriginal = ClassIndex.of(List.of(modJar), false);
        modTranslated = translatedIndex(modJar);
        adapter = new CallAdapter(modTranslated, target, conversions);
        List<String> nested = new ArrayList<>();
        String modId;
        try (ZipFile zip = new ZipFile(modJar.toFile())) {
            List<ClassNode> nodes = new ArrayList<>();
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("META-INF/jarjar/") && name.endsWith(".jar")) nested.add(name);
                if (!name.endsWith(".class") || name.startsWith("META-INF/")) continue;
                try (InputStream in = zip.getInputStream(entry)) {
                    ClassNode node = new ClassNode();
                    new ClassReader(in.readAllBytes()).accept(node, ClassReader.SKIP_FRAMES);
                    nodes.add(node);
                }
            }
            contexts = ClassContexts.find(nodes, modOriginal, vanillaOld);
            for (ClassNode node : nodes) analyzeClass(node);

            modId = modId(zip);
            String mixinConfigs = mixinConfigs(zip);
            if (mixinConfigs != null) {
                for (String config : mixinConfigs.split(",")) analyzeMixinConfig(zip, config.trim());
            }
        }
        return new Report(modJar, modId, layer.source(), modOriginal.names().size(), references, redirected, adapted,
                new ArrayList<>(findings.values()), forgeSurface, nested, contexts);
    }

    private ClassIndex translatedIndex(Path modJar) throws IOException {
        ClassIndex index = new ClassIndex(false);
        Path tmp = Files.createTempFile("rose-analyze", ".jar");
        try {
            new JarTranslator(remapper, redirects).translate(modJar, tmp);
            index.addJar(tmp);
        } finally {
            Files.deleteIfExists(tmp);
        }
        return index;
    }

    // ---- classes ----------------------------------------------------------------------------------------------

    private void analyzeClass(ClassNode node) {
        String where = node.name;
        if (node.superName != null) classRef(node.superName, where);
        for (String i : node.interfaces) classRef(i, where);
        for (FieldNode f : node.fields) descRefs(Type.getType(f.desc), where);
        for (MethodNode m : node.methods) {
            descRefs(Type.getMethodType(m.desc), where);
            checkOverride(node, m);
            for (AbstractInsnNode insn = m.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                instruction(insn, where);
            }
        }
    }

    private void instruction(AbstractInsnNode insn, String where) {
        switch (insn) {
            case MethodInsnNode mi -> {
                if (!redirect(mi, where)) {
                    Boolean isStatic = mi.getOpcode() == Opcodes.INVOKESPECIAL ? (mi.name.equals("<init>") ? Boolean.FALSE : null)
                            : Boolean.valueOf(mi.getOpcode() == Opcodes.INVOKESTATIC); // boxed: a ternary with a primitive would unbox null
                    methodRef(mi.owner, mi.name, mi.desc, where, "method", isStatic);
                }
            }
            case FieldInsnNode fi -> {
                if (!fieldRedirect(fi, where)) fieldRef(fi.owner, fi.name, fi.desc, where, "field");
            }
            case TypeInsnNode ti -> typeRef(ti.desc, where);
            case MultiANewArrayInsnNode ma -> descRefs(Type.getType(ma.desc), where);
            case LdcInsnNode ldc when ldc.cst instanceof Type t -> descRefs(t, where);
            case InvokeDynamicInsnNode indy -> {
                for (Object arg : indy.bsmArgs) {
                    if (arg instanceof Handle h) handleRef(h, where);
                    else if (arg instanceof Type t) descRefs(t, where);
                }
            }
            default -> { }
        }
    }

    /**
     * @return true if a redirect rule covers this call (it is then resolved, or reported as RULE_BROKEN when the
     *         shim is missing or has the wrong signature)
     */
    private boolean redirect(MethodInsnNode call, String where) {
                RedirectRules.Redirect rule = redirects.find(call.owner, call.name, call.desc);
        if (rule == null) return false;
        if (rule.isConstructor() && !isConstruction(call)) return false; // super(...) in a subclass can't be redirected
        references++;
        String shimDesc = remapper.mapMethodDesc(rule.shimDescriptor(call.getOpcode() == Opcodes.INVOKESTATIC));
        ClassIndex.Info shim = target.get(rule.shimOwner());
        if (shim != null && shim.methods().contains(rule.shimName() + shimDesc)) {
            redirected++;
        } else {
            String readable = rule.symbol();
            NameLayer.MemberEntry entry = layer.method(call.name);
            if (entry != null) readable = call.owner + "." + entry.readableOldName() + call.desc;
            add(Status.RULE_BROKEN, "method", rule.symbol(), readable,
                    rule.shimOwner() + "." + rule.shimName() + shimDesc + " (expected, static)", where);
        }
        return true;
    }

    /** Like {@link #redirect} for field reads. */
    private boolean fieldRedirect(FieldInsnNode field, String where) {
        if (field.getOpcode() != Opcodes.GETSTATIC && field.getOpcode() != Opcodes.GETFIELD) return false;
        RedirectRules.Redirect rule = redirects.findField(field.owner, field.name, field.desc);
        if (rule == null) return false;
        references++;
        String shimDesc = remapper.mapMethodDesc(rule.shimDescriptor(field.getOpcode() == Opcodes.GETSTATIC));
        ClassIndex.Info shim = target.get(rule.shimOwner());
        if (shim != null && shim.methods().contains(rule.shimName() + shimDesc)) {
            redirected++;
        } else {
            add(Status.RULE_BROKEN, "field", rule.symbol(), rule.symbol(),
                    rule.shimOwner() + "." + rule.shimName() + shimDesc + " (expected, static)", where);
        }
        return true;
    }

    /** Whether a constructor call creates a new object (as opposed to a subclass constructor's super(...) call). */
    private static boolean isConstruction(MethodInsnNode call) {
        int depth = 0;
        for (var insn = call.getPrevious(); insn != null; insn = insn.getPrevious()) {
            if (insn instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKESPECIAL && m.name.equals("<init>") && m.owner.equals(call.owner)) depth++;
            if (insn instanceof org.objectweb.asm.tree.TypeInsnNode t && t.getOpcode() == Opcodes.NEW && t.desc.equals(call.owner)) {
                if (depth == 0) return true;
                depth--;
            }
        }
        return false;
    }

    private void handleRef(Handle h, String where) {
        if (h.getTag() <= Opcodes.H_PUTSTATIC) fieldRef(h.getOwner(), h.getName(), h.getDesc(), where, "field");
        else methodRef(h.getOwner(), h.getName(), h.getDesc(), where, "method", null);
    }

    private void typeRef(String internalNameOrDesc, String where) {
        if (internalNameOrDesc.startsWith("[")) descRefs(Type.getType(internalNameOrDesc), where);
        else classRef(internalNameOrDesc, where);
    }

    private void descRefs(Type type, String where) {
        switch (type.getSort()) {
            case Type.ARRAY -> descRefs(type.getElementType(), where);
            case Type.OBJECT -> classRef(type.getInternalName(), where);
            case Type.METHOD -> {
                descRefs(type.getReturnType(), where);
                for (Type arg : type.getArgumentTypes()) descRefs(arg, where);
            }
            default -> { }
        }
    }

    /** @return false if the class can't be resolved (so member checks on it would only add noise) */
    private boolean classRef(String old, String where) {
        references++;
        if (modOriginal.contains(old) || startsWithAny(old, PROVIDED_PREFIXES)) return true;
        if (startsWithAny(old, FORGE_PREFIXES)) {
            forgeSurface.merge(old, 1, Integer::sum);
            if (target.contains(old)) return true; // the Forge dialect provides it; its members are checked below
            add(Status.FORGE_API, "class", old, old, "-", where);
            return false;
        }
        String mapped = remapper.map(old);
        NameLayer.ClassEntry entry = layer.classEntry(old);
        if (entry != null && entry.how() == NameLayer.How.GONE) {
            add(Status.CLASS_GONE, "class", old, old, "-", where);
            return false;
        }
        if (entry != null && entry.how() == NameLayer.How.HEURISTIC) add(Status.HEURISTIC_CLASS, "class", old, old, mapped, where);
        if (target.contains(mapped)) return true;
        if (startsWithAny(old, MINECRAFT_PREFIXES)) add(Status.CLASS_MISSING, "class", old, old, mapped, where);
        else add(Status.UNKNOWN_CLASS, "class", old, old, mapped, where);
        return false;
    }

    // ---- members ----------------------------------------------------------------------------------------------

    /** @param isStatic whether the call is static; {@code null} when unknown (no automatic adaptation then) */
    private void methodRef(String owner, String name, String desc, String where, String kind, Boolean isStatic) {
        if (owner.startsWith("[") || !classRef(owner, where) || startsWithAny(owner, PROVIDED_PREFIXES)) return;
        String newOwner = remapper.map(owner);
        String newName = remapper.mapMethodName(owner, name, desc);
        String newDesc = remapper.mapMethodDesc(desc);
        List<ClassIndex.Info> hierarchy = ClassIndex.hierarchy(newOwner, modTranslated, target);
        if (hierarchy.stream().anyMatch(i -> i.methods().contains(newName + newDesc))) return;
        if (!hierarchyComplete(newOwner)) return; // a supertype is missing; that class is already reported
        if (!signatureResolves(newDesc)) return;  // ditto for a class in the signature
        if (isStatic != null && adapter.find(newOwner, newName, newDesc, isStatic) != null) {
            adapted++; // the translator rewrites this call to the widened signature (see CallAdapter)
            return;
        }

        String symbol = owner + "." + name + desc;
        if (RosettaRemapper.SRG_METHOD.matcher(name).matches()) {
            NameLayer.MemberEntry entry = layer.method(name);
            String readableName = entry != null ? entry.readableOldName() : name;
            String readable = owner + "." + readableName + desc;
            if (entry == null || !entry.exists()) {
                boolean sameNameExists = hasMethodNamed(hierarchy, readableName);
                add(sameNameExists ? Status.SIGNATURE_CHANGED : Status.METHOD_GONE, kind, symbol, readable,
                        sameNameExists ? newOwner + "." + readableName + " (new signature)" : "-", where);
            } else if (hasMethodNamed(hierarchy, newName)) {
                add(Status.SIGNATURE_CHANGED, kind, symbol, readable, newOwner + "." + newName + newDesc, where);
            } else {
                add(Status.METHOD_MISSING, kind, symbol, readable, newOwner + "." + newName + newDesc, where);
            }
        } else if (startsWithAny(owner, FORGE_PREFIXES)) {
            add(Status.FORGE_API, kind, symbol, symbol, "-", where);
        } else if (existsInVanillaOld(owner, name + desc)) {
            add(hasMethodNamed(hierarchy, name) ? Status.SIGNATURE_CHANGED : Status.METHOD_MISSING,
                    kind, symbol, symbol, newOwner + "." + newName + newDesc, where);
        } else if (startsWithAny(owner, MINECRAFT_PREFIXES) || isMinecraftSubclass(owner)) {
            forgeSurface.merge(owner + "." + name, 1, Integer::sum);
            add(Status.FORGE_EXTENSION, kind, symbol, symbol, "-", where);
        }
    }

    private void fieldRef(String owner, String name, String desc, String where, String kind) {
        if (owner.startsWith("[") || !classRef(owner, where) || startsWithAny(owner, PROVIDED_PREFIXES)) return;
        String newOwner = remapper.map(owner);
        String newName = remapper.mapFieldName(owner, name, desc);
        String newDesc = remapper.mapDesc(desc);
        List<ClassIndex.Info> hierarchy = ClassIndex.hierarchy(newOwner, modTranslated, target);
        if (hierarchy.stream().anyMatch(i -> i.fields().contains(newName + ":" + newDesc))) return;
        if (!hierarchyComplete(newOwner) || !signatureResolves(newDesc)) return;
        if (adapter.findField(newOwner, newName, newDesc, true) != null) {
            adapted++; // read through a conversion (e.g. the field became a Holder)
            return;
        }

        String symbol = owner + "." + name + ":" + desc;
        if (RosettaRemapper.SRG_FIELD.matcher(name).matches()) {
            NameLayer.MemberEntry entry = layer.field(name);
            String readableName = entry != null ? entry.readableOldName() : name;
            String readable = owner + "." + readableName + ":" + desc;
            String lookFor = entry != null && entry.exists() ? newName : readableName;
            if (hasFieldNamed(hierarchy, lookFor)) {
                add(Status.SIGNATURE_CHANGED, kind, symbol, readable, newOwner + "." + lookFor + " (new type)", where);
            } else if (entry == null || !entry.exists()) {
                add(Status.FIELD_GONE, kind, symbol, readable, "-", where);
            } else {
                add(Status.FIELD_MISSING, kind, symbol, readable, newOwner + "." + newName + ":" + newDesc, where);
            }
        } else if (startsWithAny(owner, FORGE_PREFIXES)) {
            add(Status.FORGE_API, kind, symbol, symbol, "-", where);
        } else if (startsWithAny(owner, MINECRAFT_PREFIXES) || isMinecraftSubclass(owner)) {
            boolean typeChanged = hasFieldNamed(hierarchy, name);
            Status status = typeChanged ? Status.SIGNATURE_CHANGED
                    : existsInVanillaOld(owner, null) ? Status.FIELD_MISSING : Status.FORGE_EXTENSION;
            add(status, kind, symbol, symbol, typeChanged ? newOwner + "." + name + " (new type)" : "-", where);
        }
    }

    /** A mod method with an SRG name overrides a vanilla method; check that method still exists to override. */
    private void checkOverride(ClassNode node, MethodNode m) {
        if (!RosettaRemapper.SRG_METHOD.matcher(m.name).matches()) return;
        if ((m.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) != 0) return;
        NameLayer.MemberEntry entry = layer.method(m.name);
        String readableName = entry != null ? entry.readableOldName() : m.name;
        String symbol = node.name + "." + m.name + m.desc;
        String readable = node.name + "." + readableName + m.desc;
        String newOwner = remapper.map(node.name);
        String newDesc = remapper.mapMethodDesc(m.desc);
        List<ClassIndex.Info> supers = ClassIndex.hierarchy(newOwner, modTranslated, target);
        List<ClassIndex.Info> above = supers.size() > 1 ? supers.subList(1, supers.size()) : List.of();
        if (!signatureResolves(newDesc) || !hierarchyComplete(newOwner)) return; // root cause reported elsewhere
        if (entry == null || !entry.exists()) {
            boolean sameNameExists = hasMethodNamed(above, readableName);
            add(sameNameExists ? Status.OVERRIDE_SIGNATURE_CHANGED : Status.OVERRIDE_GONE, "override", symbol, readable,
                    sameNameExists ? readableName + " (new signature)" : "-", node.name);
            return;
        }
        boolean overridden = above.stream().anyMatch(i -> i.methods().contains(entry.newName() + newDesc));
        if (!overridden) {
            add(Status.OVERRIDE_SIGNATURE_CHANGED, "override", symbol, readable, newOwner + "." + entry.newName() + newDesc, node.name);
        }
    }

    private static boolean hasMethodNamed(List<ClassIndex.Info> hierarchy, String name) {
        return hierarchy.stream().anyMatch(i -> i.methodNames().contains(name));
    }

    private static boolean hasFieldNamed(List<ClassIndex.Info> hierarchy, String name) {
        return hierarchy.stream().anyMatch(i -> i.fields().contains(name));
    }

    private boolean hierarchyComplete(String newOwner) {
        for (ClassIndex.Info info : ClassIndex.hierarchy(newOwner, modTranslated, target)) {
            if (info.superName() != null && !modTranslated.contains(info.superName()) && !target.contains(info.superName())) return false;
            for (String i : info.interfaces()) if (!modTranslated.contains(i) && !target.contains(i)) return false;
        }
        return modTranslated.contains(newOwner) || target.contains(newOwner);
    }

    private boolean existsInVanillaOld(String owner, String methodNameAndDesc) {
        List<ClassIndex.Info> h = ClassIndex.hierarchy(owner, modOriginal, vanillaOld);
        if (methodNameAndDesc == null) return h.stream().anyMatch(i -> vanillaOld.contains(i.name()));
        return h.stream().anyMatch(i -> vanillaOld.contains(i.name()) && i.methods().contains(methodNameAndDesc));
    }

    private boolean isMinecraftSubclass(String owner) {
        return ClassIndex.hierarchy(owner, modOriginal, vanillaOld).stream()
                .anyMatch(i -> startsWithAny(i.name(), MINECRAFT_PREFIXES));
    }

    // ---- mixins -----------------------------------------------------------------------------------------------

    private void analyzeMixinConfig(ZipFile zip, String configName) throws IOException {
        JsonObject config = json(zip, configName);
        if (config == null) return;
        JsonObject refmap = config.has("refmap") ? json(zip, config.get("refmap").getAsString()) : null;
        JsonObject mappings = refmap != null && refmap.has("mappings") ? refmap.getAsJsonObject("mappings") : new JsonObject();

        String pkg = config.get("package").getAsString().replace('.', '/');
        for (String list : List.of("mixins", "client", "server")) {
            if (!config.has(list)) continue;
            for (JsonElement e : config.getAsJsonArray(list)) {
                String mixin = pkg + "/" + e.getAsString().replace('.', '/');
                mixinTargets(zip, mixin);
                JsonObject refs = mappings.has(mixin) ? mappings.getAsJsonObject(mixin) : new JsonObject();
                for (var ref : refs.entrySet()) mixinMember(ref.getValue().getAsString(), mixin);
            }
        }
    }

    private void mixinTargets(ZipFile zip, String mixin) throws IOException {
        ZipEntry entry = zip.getEntry(mixin + ".class");
        if (entry == null) return;
        ClassNode node = new ClassNode();
        try (InputStream in = zip.getInputStream(entry)) {
            new ClassReader(in.readAllBytes()).accept(node, ClassReader.SKIP_CODE);
        }
        List<AnnotationNode> annotations = new ArrayList<>();
        if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);
        if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
        for (AnnotationNode a : annotations) {
            if (!a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || a.values == null) continue;
            for (int i = 0; i < a.values.size(); i += 2) {
                if (!(a.values.get(i + 1) instanceof List<?> list)) continue;
                for (Object value : list) {
                    String name = value instanceof Type t ? t.getInternalName() : value.toString().replace('.', '/');
                    classRef(name, mixin); // problems are reported with the mixin as the user
                }
            }
        }
    }

    /** Refmap entries look like {@code Lowner;name(desc)ret} (methods) or {@code Lowner;name:desc} (fields). */
    private void mixinMember(String ref, String mixin) {
        if (!ref.startsWith("L") || !ref.contains(";")) return;
        String owner = ref.substring(1, ref.indexOf(';'));
        String rest = ref.substring(ref.indexOf(';') + 1);
        if (rest.contains("(")) {
            methodRef(owner, rest.substring(0, rest.indexOf('(')), rest.substring(rest.indexOf('(')), mixin, "mixin-member", null);
        } else if (rest.contains(":")) {
            fieldRef(owner, rest.substring(0, rest.indexOf(':')), rest.substring(rest.indexOf(':') + 1), mixin, "mixin-member");
        }
    }

    // ---- helpers ----------------------------------------------------------------------------------------------

    private void add(Status status, String kind, String symbol, String readable, String target, String where) {
        Finding f = findings.computeIfAbsent(status + "|" + kind + "|" + symbol,
                k -> new Finding(status, kind, symbol, readable, target));
        f.count++;
        f.usedIn.add(where);
        if (contexts.runtime(where)) f.runtime = true;
        else if (contexts.integration().contains(where)) f.integration = true;
    }

    /**
     * A member whose (translated) signature mentions a class that doesn't resolve is a consequence of that class
     * problem, which is already reported; listing every such member would bury the root cause.
     */
    private boolean signatureResolves(String newDesc) {
        for (Type t : newDesc.startsWith("(") ? argsAndReturn(Type.getMethodType(newDesc)) : List.of(Type.getType(newDesc))) {
            Type element = t.getSort() == Type.ARRAY ? t.getElementType() : t;
            if (element.getSort() != Type.OBJECT) continue;
            String name = element.getInternalName();
            if (!modTranslated.contains(name) && !target.contains(name)) return false;
        }
        return true;
    }

    private static List<Type> argsAndReturn(Type method) {
        List<Type> out = new ArrayList<>(List.of(method.getArgumentTypes()));
        out.add(method.getReturnType());
        return out;
    }

    static boolean startsWithAny(String name, List<String> prefixes) {
        for (String p : prefixes) if (name.startsWith(p)) return true;
        return false;
    }

    private static JsonObject json(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) return null;
        try (InputStream in = zip.getInputStream(entry)) {
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static String mixinConfigs(ZipFile zip) throws IOException {
        ZipEntry manifest = zip.getEntry("META-INF/MANIFEST.MF");
        if (manifest == null) return null;
        try (InputStream in = zip.getInputStream(manifest)) {
            return new Manifest(in).getMainAttributes().getValue("MixinConfigs");
        }
    }

    private static String modId(ZipFile zip) throws IOException {
        ZipEntry toml = zip.getEntry("META-INF/mods.toml");
        if (toml == null) return null;
        try (InputStream in = zip.getInputStream(toml)) {
            var m = Pattern.compile("modId\\s*=\\s*\"([^\"]+)\"").matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return m.find() ? m.group(1) : null;
        }
    }

    static JsonArray strings(Iterable<String> values) {
        JsonArray out = new JsonArray();
        values.forEach(out::add);
        return out;
    }
}

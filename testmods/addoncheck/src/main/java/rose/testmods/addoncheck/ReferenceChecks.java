package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.test;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.gametest.framework.GameTestHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/**
 * Every field and method a translated mod's code uses resolves on 26.3, with the access of the class using it. The
 * linkage gate (LinkageChecks) resolves classes by their own method signatures; a reference inside a method body
 * fails only when that code first runs: a Forge class Rose doesn't provide, a member gone in 26.3, a field that
 * Forge's access transformer made public and 26.3 keeps protected. One bad reference in an event listener takes the
 * whole listener class down the first time the event fires.
 *
 * <p>Known ones (references-known.tsv, member and reason) don't fail the test until they resolve, when their entry
 * must go. Missing classes that aren't the game's (another mod's optional API) and data generation never fail it.
 */
final class ReferenceChecks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ReferenceChecks() {}

    static void register() {
        test("translated_mod_references_resolve", ReferenceChecks::referencesResolve);
    }

    private record Ref(int opcode, String owner, String name, String desc) {
        String member() {
            return owner + "." + name + desc;
        }
    }

    private static void referencesResolve(GameTestHelper helper) {
        Map<String, String> known = known();
        Map<String, Set<String>> problems = new TreeMap<>();
        Map<String, String> errors = new HashMap<>();
        Set<String> stillBroken = new LinkedHashSet<>();
        int checked = 0;
        for (ModMetadata mod : RoseLoader.get().mods()) {
            if (mod.isNative()) continue;
            Set<String> mixinPackages = LinkageChecks.mixinPackages(mod);
            try (ZipFile zip = new ZipFile(mod.root().toFile())) {
                for (ZipEntry e : Collections.list(zip.entries())) {
                    String entry = e.getName();
                    if (!entry.endsWith(".class") || entry.startsWith("META-INF/") || entry.endsWith("module-info.class")
                            || entry.endsWith("package-info.class")) continue;
                    String name = entry.substring(0, entry.length() - 6).replace('/', '.');
                    if (mixinPackages.stream().anyMatch(name::startsWith)) continue;
                    Class<?> from = load(name);
                    if (from == null) continue; // doesn't link: the linkage gate reports it
                    MethodHandles.Lookup lookup;
                    try {
                        lookup = MethodHandles.privateLookupIn(from, MethodHandles.lookup());
                    } catch (IllegalAccessException | LinkageError ex) {
                        continue;
                    }
                    for (Ref ref : refs(zip.getInputStream(e).readAllBytes())) {
                        checked++;
                        String error = resolve(ref, from, lookup);
                        if (error == null) continue;
                        if (known.containsKey(ref.member())) {
                            stillBroken.add(ref.member());
                            continue;
                        }
                        problems.computeIfAbsent(ref.member(), k -> new java.util.TreeSet<>()).add(mod.id() + ": " + name);
                        errors.put(ref.member(), error);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Can't read " + mod.root(), ex);
            }
        }
        List<String> failures = new ArrayList<>();
        problems.forEach((member, users) -> failures.add(member + ": " + errors.get(member) + " (used in " + String.join(", ", users) + ")"));
        for (String member : known.keySet()) {
            if (!stillBroken.contains(member)) failures.add(member + " resolves now (or is no longer used): remove it from references-known.tsv");
        }
        failures.forEach(f -> LOGGER.warn("[references] {}", f));
        LOGGER.info("[references] {} references checked, {} known not to resolve, {} problems", checked, stillBroken.size(), failures.size());
        helper.assertTrue(failures.isEmpty(), failures.size() + " translated references don't resolve:\n" + String.join("\n", failures));
        helper.succeed();
    }

    private static Class<?> load(String name) {
        try {
            Class<?> type = Class.forName(name, false, RoseLoader.get().classLoader());
            type.getDeclaredMethods(); // links the signatures, as the linkage gate does
            return type;
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    /** Null if {@code ref} resolves from {@code from}, or a problem that translation should fix; else the error. */
    private static String resolve(Ref ref, Class<?> from, MethodHandles.Lookup lookup) {
        if (ref.owner().startsWith("[")) return null; // array clone and the like
        if (ref.owner().startsWith("net/minecraft/data/")) return null; // data generation, never run in game
        ClassLoader loader = RoseLoader.get().classLoader();
        try {
            Class<?> owner = Class.forName(ref.owner().replace('/', '.'), false, loader);
            switch (ref.opcode()) {
                case Opcodes.GETSTATIC, Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.PUTFIELD -> {
                    Class<?> type = MethodType.fromMethodDescriptorString("()" + ref.desc(), loader).returnType();
                    boolean isStatic = ref.opcode() == Opcodes.GETSTATIC || ref.opcode() == Opcodes.PUTSTATIC;
                    if (isStatic) lookup.findStaticGetter(owner, ref.name(), type);
                    else lookup.findGetter(owner, ref.name(), type);
                    boolean put = ref.opcode() == Opcodes.PUTSTATIC || ref.opcode() == Opcodes.PUTFIELD;
                    Field field = field(owner, ref.name());
                    if (put && field != null && Modifier.isFinal(field.getModifiers()) && field.getDeclaringClass() != from) {
                        return "final field written from outside its class";
                    }
                }
                case Opcodes.INVOKESTATIC -> lookup.findStatic(owner, ref.name(), MethodType.fromMethodDescriptorString(ref.desc(), loader));
                case Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE -> lookup.findVirtual(owner, ref.name(), MethodType.fromMethodDescriptorString(ref.desc(), loader));
                case Opcodes.INVOKESPECIAL -> {
                    MethodType type = MethodType.fromMethodDescriptorString(ref.desc(), loader);
                    if (!ref.name().equals("<init>")) lookup.findSpecial(owner, ref.name(), type, from);
                    else if (owner != from && owner.isAssignableFrom(from)) owner.getDeclaredConstructor(type.parameterArray()); // super(...): a subclass may call a protected constructor
                    else lookup.findConstructor(owner, type);
                }
                default -> {
                    return null;
                }
            }
            return null;
        } catch (ReflectiveOperationException | LinkageError | TypeNotPresentException e) {
            Throwable cause = e instanceof TypeNotPresentException && e.getCause() != null ? e.getCause() : e;
            if (String.valueOf(cause.getMessage()).contains("caller-sensitive")) return null; // no lookup may find those; bytecode can call them
            return isTranslationProblem(cause) ? cause.toString() : null;
        }
    }

    /** A missing class counts only if it's the game's (not data generation); anything else always counts. */
    private static boolean isTranslationProblem(Throwable problem) {
        String missing;
        if (problem instanceof NoClassDefFoundError) missing = String.valueOf(problem.getMessage());
        else if (problem instanceof ClassNotFoundException) missing = String.valueOf(problem.getMessage()).replace('.', '/');
        else return true;
        return (missing.startsWith("net/minecraft/") || missing.startsWith("com/mojang/")) && !missing.startsWith("net/minecraft/data/");
    }

    private static Field field(Class<?> owner, String name) {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException | LinkageError e) {
                // keep looking up
            }
        }
        return null;
    }

    private static Set<Ref> refs(byte[] bytes) {
        Set<Ref> out = new LinkedHashSet<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDesc) {
                        out.add(new Ref(opcode, owner, fieldName, fieldDesc));
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDesc, boolean isInterface) {
                        out.add(new Ref(opcode, owner, methodName, methodDesc));
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return out;
    }

    /** references-known.tsv: member ({@code owner.name desc}) to why it doesn't resolve yet. */
    private static Map<String, String> known() {
        Map<String, String> out = new HashMap<>();
        try (var in = ReferenceChecks.class.getResourceAsStream("/addoncheck/references-known.tsv")) {
            if (in == null) throw new IllegalStateException("references-known.tsv is missing");
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] p = line.split("\t");
                if (p.length < 2 || p[1].isBlank()) throw new IllegalStateException("references-known.tsv: a member needs a reason: " + line);
                out.put(p[0], p[1]);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out;
    }
}

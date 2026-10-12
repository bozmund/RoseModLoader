package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.test;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Every class of every translated mod links on 26.3. A class whose bytecode doesn't verify (a 1.20.1 type that is
 * gone, passed where another is expected; an override of a method that became final) fails as a whole the first
 * time anything resolves a member of it, and takes every method with it: a listener class loses all its listeners,
 * though only one method was wrong.
 *
 * <p>Every class that doesn't link is logged. Known ones (linkage-known.tsv, each with its reason) don't fail the
 * test until they link, when their entry must go. The test fails for the ones translation should have fixed: those
 * missing a Minecraft class (outside data generation, which never runs in game) or broken in another way. Classes
 * missing a Forge API class are listed but don't fail it (the event bus reports those as unsupported), nor do
 * classes missing another mod's API (optional integrations, loaded only when that mod is).
 */
final class LinkageChecks {
    private static final Logger LOGGER = LogUtils.getLogger();

    private LinkageChecks() {}

    static void register() {
        test("translated_mod_classes_link", LinkageChecks::translatedClassesLink);
    }

    private record Method(String name, String desc, boolean isStatic) {}

    private static void translatedClassesLink(GameTestHelper helper) {
        List<String> translation = new ArrayList<>();
        java.util.Map<String, String> known = known();
        Set<String> knownChecked = new HashSet<>();
        int checked = 0;
        int broken = 0;
        for (ModMetadata mod : RoseLoader.get().mods()) {
            if (mod.isNative()) continue;
            Set<String> mixinPackages = mixinPackages(mod);
            try (ZipFile zip = new ZipFile(mod.root().toFile())) {
                for (ZipEntry e : java.util.Collections.list(zip.entries())) {
                    String entry = e.getName();
                    if (!entry.endsWith(".class") || entry.startsWith("META-INF/") || entry.endsWith("module-info.class")
                            || entry.endsWith("package-info.class")) continue;
                    String name = entry.substring(0, entry.length() - 6).replace('/', '.');
                    if (mixinPackages.stream().anyMatch(name::startsWith)) continue;
                    checked++;
                    Throwable problem = link(name, methods(zip.getInputStream(e).readAllBytes()));
                    if (known.containsKey(name)) knownChecked.add(name);
                    if (problem == null) {
                        if (known.containsKey(name)) translation.add(mod.id() + ": " + name + " links now: remove it from linkage-known.tsv");
                        continue;
                    }
                    broken++;
                    String line = mod.id() + ": " + name + ": " + problem;
                    LOGGER.warn("[linkage] {}", line);
                    if (isTranslationProblem(problem) && !known.containsKey(name)) translation.add(line);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Can't read " + mod.root(), ex);
            }
        }
        LOGGER.info("[linkage] {} translated classes checked, {} don't link ({} known), {} new translation problems",
                checked, broken, knownChecked.size(), translation.size());
        helper.assertTrue(translation.isEmpty(), translation.size() + " translated classes don't link:\n" + String.join("\n", translation));
        helper.succeed();
    }

    /** linkage-known.tsv: class to why it doesn't link yet. */
    private static java.util.Map<String, String> known() {
        java.util.Map<String, String> out = new java.util.HashMap<>();
        try (var in = LinkageChecks.class.getResourceAsStream("/addoncheck/linkage-known.tsv")) {
            if (in == null) throw new IllegalStateException("linkage-known.tsv is missing");
            for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\n")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] p = line.split("\t");
                if (p.length < 2 || p[1].isBlank()) throw new IllegalStateException("linkage-known.tsv: a class needs a reason: " + line);
                out.put(p[0], p[1]);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out;
    }

    /** Null if the class links, else the error. Linking is forced by resolving one of its methods (as event buses do). */
    private static Throwable link(String name, List<Method> methods) {
        ClassLoader loader = RoseLoader.get().classLoader();
        Class<?> type;
        MethodHandles.Lookup lookup;
        try {
            type = Class.forName(name, false, loader);
            lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
        } catch (ReflectiveOperationException | LinkageError e) {
            return e;
        }
        for (Method m : methods) {
            MethodType methodType;
            try {
                methodType = MethodType.fromMethodDescriptorString(m.desc(), loader);
            } catch (TypeNotPresentException | LinkageError e) {
                continue; // a type in this method's signature is missing: the method is unusable, the class may link
            }
            try {
                if (m.name().equals("<init>")) lookup.findConstructor(type, methodType);
                else if (m.isStatic()) lookup.findStatic(type, m.name(), methodType);
                else lookup.findVirtual(type, m.name(), methodType);
                return null;
            } catch (ReflectiveOperationException | LinkageError e) {
                return e.getCause() instanceof LinkageError cause ? cause : e;
            }
        }
        return null; // no method whose signature resolves: nothing to link it by
    }

    /** A missing Minecraft class (not data generation), or any error other than a missing class. */
    private static boolean isTranslationProblem(Throwable problem) {
        if (!(problem instanceof NoClassDefFoundError)) return true;
        String missing = String.valueOf(problem.getMessage());
        return (missing.startsWith("net/minecraft/") || missing.startsWith("com/mojang/")) && !missing.startsWith("net/minecraft/data/");
    }

    private static List<Method> methods(byte[] bytes) {
        List<Method> out = new ArrayList<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (!name.equals("<clinit>") && (access & Opcodes.ACC_SYNTHETIC) == 0) out.add(new Method(name, desc, (access & Opcodes.ACC_STATIC) != 0));
                return null;
            }
        }, ClassReader.SKIP_CODE);
        return out;
    }

    /** Packages of the mod's mixin configs: mixin classes are applied to targets, never loaded themselves. */
    static Set<String> mixinPackages(ModMetadata mod) {
        Set<String> out = new HashSet<>();
        try (ZipFile zip = new ZipFile(mod.root().toFile())) {
            for (String config : mod.mixins()) {
                ZipEntry e = zip.getEntry(config);
                if (e == null) continue;
                var json = com.google.gson.JsonParser.parseString(new String(zip.getInputStream(e).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                if (json.getAsJsonObject().has("package")) out.add(json.getAsJsonObject().get("package").getAsString() + ".");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't read " + mod.root(), e);
        }
        return out;
    }
}

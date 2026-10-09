package rose.boot.forge;

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
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import rose.loader.ModMetadata;
import rose.rosetta.NameLayer;
import rose.rosetta.RedirectRules;
import rose.translate.ClassIndex;
import rose.translate.ModTranslator;
import rose.translate.forge.TypedListenerTransform;

/**
 * Finds Forge 1.20.1 mods (jars with {@code META-INF/mods.toml}) in the mods folder, translates each one to 26.3
 * (cached) and describes it as a {@link ModMetadata} with dialect {@value #DIALECT}. The Forge dialect mod reads the
 * entrypoints: {@code forge:mod} (classes annotated {@code @Mod}) and {@code forge:subscriber}
 * ({@code @Mod.EventBusSubscriber}).
 */
public final class ForgeMods {
    public static final String DIALECT = "forge-1.20.1";
    public static final String MOD_ENTRYPOINT = "forge:mod";
    public static final String SUBSCRIBER_ENTRYPOINT = "forge:subscriber";
    private static final String MOD_ANNOTATION = "Lnet/minecraftforge/fml/common/Mod;";
    private static final String SUBSCRIBER_ANNOTATION = "Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;";

    private final Path nameLayer;
    private final Path redirects;
    private final List<Path> gameJars;
    private final Path cacheDir;

    /**
     * @param gameJars the game, its libraries and Rose's own mods: what translated code will link against
     */
    public ForgeMods(Path nameLayer, Path redirects, List<Path> gameJars, Path cacheDir) {
        this.nameLayer = nameLayer;
        this.redirects = redirects;
        this.gameJars = List.copyOf(gameJars);
        this.cacheDir = cacheDir;
    }

    /** Jars in {@code modsDir} that are Forge mods (and not Rose-native ones). */
    public static List<Path> find(Path modsDir) throws IOException {
        List<Path> out = new ArrayList<>();
        if (!Files.isDirectory(modsDir)) return out;
        try (Stream<Path> files = Files.list(modsDir)) {
            for (Path jar : files.filter(p -> p.getFileName().toString().endsWith(".jar")).sorted().toList()) {
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    if (zip.getEntry("META-INF/mods.toml") != null && zip.getEntry(ModMetadata.FILE_NAME) == null) out.add(jar);
                }
            }
        }
        return out;
    }

    public List<ModMetadata> load(List<Path> jars) throws IOException {
        if (jars.isEmpty()) return List.of();
        if (!Files.exists(nameLayer)) {
            throw new IOException("Forge mods found but the Rosetta name layer is missing: " + nameLayer
                    + " (run ./gradlew :rosetta:buildNameLayers)");
        }
        long start = System.nanoTime();
        String inputsHash = ModTranslator.sha256(concat(Files.readAllBytes(nameLayer),
                Files.exists(redirects) ? Files.readAllBytes(redirects) : new byte[0]));
        NameLayer layer = NameLayer.read(nameLayer);
        RedirectRules rules = RedirectRules.read(redirects);
        ClassIndex game = ClassIndex.of(gameJars, true);
        ModTranslator translator = new ModTranslator(layer, rules, game, inputsHash, List.of(new TypedListenerTransform()));

        List<ModMetadata> mods = new ArrayList<>();
        for (Path jar : jars) {
            Path translated = translator.translate(jar, cacheDir);
            mods.add(describe(jar, translated));
        }
        System.out.printf("[rose] translated %d Forge mod(s) in %d ms%n", mods.size(), (System.nanoTime() - start) / 1_000_000);
        return mods;
    }

    static ModMetadata describe(Path original, Path translated) throws IOException {
        try (ZipFile zip = new ZipFile(translated.toFile())) {
            Map<String, String> toml = firstMod(read(zip, "META-INF/mods.toml"));
            Manifest manifest = manifest(zip);
            String id = toml.get("modId");
            if (id == null) throw new IOException(original + ": mods.toml has no [[mods]] modId");
            String version = toml.getOrDefault("version", "0");
            if (version.contains("${file.jarVersion}") && manifest != null) {
                String v = manifest.getMainAttributes().getValue("Implementation-Version");
                version = v != null ? v : "0";
            }
            List<String> mixins = new ArrayList<>();
            if (manifest != null && manifest.getMainAttributes().getValue("MixinConfigs") != null) {
                for (String c : manifest.getMainAttributes().getValue("MixinConfigs").split(",")) {
                    if (!c.isBlank()) mixins.add(c.trim());
                }
            }
            Map<String, List<String>> entrypoints = new HashMap<>();
            scanAnnotations(zip, entrypoints);
            return new ModMetadata(id, toml.getOrDefault("displayName", id), version, List.copyOf(mixins),
                    Map.copyOf(entrypoints), translated, DIALECT, original);
        }
    }

    private static void scanAnnotations(ZipFile zip, Map<String, List<String>> entrypoints) throws IOException {
        var entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.getName().endsWith(".class")) continue;
            byte[] bytes;
            try (InputStream in = zip.getInputStream(e)) {
                bytes = in.readAllBytes();
            }
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                private String name;

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    this.name = name.replace('/', '.');
                }

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (desc.equals(MOD_ANNOTATION)) entrypoints.computeIfAbsent(MOD_ENTRYPOINT, k -> new ArrayList<>()).add(name);
                    if (desc.equals(SUBSCRIBER_ANNOTATION)) entrypoints.computeIfAbsent(SUBSCRIBER_ENTRYPOINT, k -> new ArrayList<>()).add(name);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        entrypoints.replaceAll((k, v) -> List.copyOf(v.stream().sorted().toList()));
    }

    /**
     * The first {@code [[mods]]} table of a mods.toml, single-line string/number values only (all Forge reads at
     * this stage). Multi-line strings ({@code '''...'''}) are skipped.
     */
    static Map<String, String> firstMod(String toml) {
        Map<String, String> out = new LinkedHashMap<>();
        boolean inMods = false;
        boolean inMultiline = false;
        for (String raw : toml.split("\\R")) {
            String line = raw.strip();
            if (inMultiline) {
                if (line.contains("'''") || line.contains("\"\"\"")) inMultiline = false;
                continue;
            }
            if (line.startsWith("[")) {
                if (inMods && !out.isEmpty()) break;
                inMods = line.equals("[[mods]]");
                continue;
            }
            if (!inMods || line.startsWith("#") || !line.contains("=")) continue;
            String key = line.substring(0, line.indexOf('=')).strip();
            String value = line.substring(line.indexOf('=') + 1).strip();
            if (value.startsWith("'''") || value.startsWith("\"\"\"")) {
                inMultiline = value.length() < 6 || !(value.endsWith("'''") || value.endsWith("\"\"\""));
                continue;
            }
            if (value.startsWith("\"") && value.indexOf('"', 1) > 0) value = value.substring(1, value.indexOf('"', 1));
            else if (value.startsWith("'") && value.indexOf('\'', 1) > 0) value = value.substring(1, value.indexOf('\'', 1));
            else if (value.contains("#")) value = value.substring(0, value.indexOf('#')).strip();
            out.put(key, value);
        }
        return out;
    }

    private static String read(ZipFile zip, String name) throws IOException {
        ZipEntry e = zip.getEntry(name);
        if (e == null) throw new IOException(zip.getName() + " has no " + name);
        try (InputStream in = zip.getInputStream(e)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Manifest manifest(ZipFile zip) throws IOException {
        ZipEntry e = zip.getEntry("META-INF/MANIFEST.MF");
        if (e == null) return null;
        try (InputStream in = zip.getInputStream(e)) {
            return new Manifest(in);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}

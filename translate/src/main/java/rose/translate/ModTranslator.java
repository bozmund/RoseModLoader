package rose.translate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import com.google.gson.JsonParser;
import rose.rosetta.BridgeRules;
import rose.rosetta.ConversionRules;
import rose.rosetta.NameLayer;
import rose.rosetta.RedirectRules;

/**
 * Produces the 26.3 version of an old mod jar, once, and caches it by content: renames (Rosetta), redirects to
 * era-bridge shims, adapted calls, remapped Mixin refmaps and dialect-specific rewrites. Nested jars are dropped
 * (libraries Forge mods bundle, like MixinExtras, are provided by Rose).
 */
public final class ModTranslator {
    /** Bump when translation output changes, so cached jars are rebuilt. */
    public static final int VERSION = 23;

    private final NameLayer layer;
    private final RedirectRules redirects;
    private final BridgeRules bridges;
    private final ConversionRules conversions;
    private final ClassIndex game;
    private final String inputsHash;
    private final List<ExtraTransform> extras;

    /** Rewrites a non-class resource (data/asset upgrades): the new path and content, or {@code null} to leave it out. */
    @FunctionalInterface
    public interface ResourceTransform {
        Resource apply(String path, byte[] content);

        /** Resources to add after every original one was seen (e.g. files the new version needs and the old lacked). */
        default List<Resource> extras() {
            return List.of();
        }
    }

    public record Resource(String path, byte[] content) {}

    /** A dialect-specific rewrite that runs after renaming (e.g. typed event listeners for Forge). */
    @FunctionalInterface
    public interface ExtraTransform {
        org.objectweb.asm.ClassVisitor wrap(org.objectweb.asm.ClassVisitor next);
    }

    /**
     * @param game       the 26.3 game, its libraries and Rose's built-in mods (for call adaptation)
     * @param inputsHash identifies the rule set (name layer + rule files) so cached jars follow rule changes
     */
    public ModTranslator(NameLayer layer, RedirectRules redirects, BridgeRules bridges, ConversionRules conversions,
                         ClassIndex game, String inputsHash, List<ExtraTransform> extras) {
        this.layer = layer;
        this.redirects = redirects;
        this.bridges = bridges;
        this.conversions = conversions;
        this.game = game;
        this.inputsHash = inputsHash;
        this.extras = List.copyOf(extras);
    }

    /** The translated jar for {@code input} inside {@code cacheDir}, building it if needed. */
    public Path translate(Path input, Path cacheDir) throws IOException {
        return translate(input, cacheDir, (path, content) -> new Resource(path, content));
    }

    /** As {@link #translate(Path, Path)}, upgrading other resources with {@code resources}. */
    public Path translate(Path input, Path cacheDir, ResourceTransform resources) throws IOException {
        String key = sha256(Files.readAllBytes(input)).substring(0, 16) + "-" + inputsHash.substring(0, 12) + "-v" + VERSION;
        Path output = cacheDir.resolve(input.getFileName().toString().replaceAll("\\.jar$", "") + "-" + key + ".jar");
        if (Files.exists(output)) return output;
        Files.createDirectories(cacheDir);

        RosettaRemapper remapper = new RosettaRemapper(layer);
        // Pass 1: rename only, to learn the mod's own (translated) class hierarchy for call adaptation.
        Path renamed = Files.createTempFile(cacheDir, "pass1-", ".jar");
        try {
            new JarTranslator(remapper, redirects).translate(input, renamed);
            ClassIndex mod = ClassIndex.of(List.of(renamed), false);
            CallAdapter adapter = new CallAdapter(mod, game, conversions);
            List<ExtraTransform> all = new ArrayList<>(extras);
            all.add(new InheritanceBridger(bridges, mod, game));
            JarTranslator full = new JarTranslator(remapper, redirects, adapter, all);
            Path tmp = output.resolveSibling(output.getFileName() + ".part");
            writeTranslated(input, tmp, full, remapper, resources);
            Files.move(tmp, output, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(renamed);
        }
        return output;
    }

    private void writeTranslated(Path input, Path output, JarTranslator translator, RosettaRemapper remapper,
                                 ResourceTransform resources) throws IOException {
        List<String> mixinConfigs = mixinConfigsOf(input);
        List<String> refmaps = refmapsOf(input);
        RefmapRemapper refmapRemapper = new RefmapRemapper(remapper);
        java.util.Set<String> written = new java.util.HashSet<>();
        try (ZipFile in = new ZipFile(input.toFile()); ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output))) {
            var entries = in.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || name.startsWith("META-INF/jarjar/") || isSignature(name)) continue;
                byte[] bytes;
                try (InputStream stream = in.getInputStream(entry)) {
                    bytes = stream.readAllBytes();
                }
                if (name.endsWith(".class")) bytes = translator.translateClass(bytes);
                else if (mixinConfigs.contains(name)) bytes = tolerantMixinConfig(new String(bytes, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
                else if (refmaps.contains(name)) bytes = refmapRemapper.remap(new String(bytes, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
                else {
                    Resource fixed = resources.apply(name, bytes);
                    if (fixed == null) continue;
                    name = fixed.path();
                    if (fixed.content() != null) bytes = fixed.content();
                }
                if (!written.add(name)) continue; // two old files mapped to one new path: the first wins
                out.putNextEntry(new ZipEntry(name));
                out.write(bytes);
                out.closeEntry();
            }
            for (Resource extra : resources.extras()) {
                if (!written.add(extra.path())) continue; // the mod has its own
                out.putNextEntry(new ZipEntry(extra.path()));
                out.write(extra.content());
                out.closeEntry();
            }
        }
    }

    /**
     * Until the Mixin rebaser checks each mixin against 26.3, an old mixin that no longer fits must not take the
     * game down: the config becomes optional and injectors may find no target. Mixin then logs and skips what
     * doesn't apply, and the rest of the mod still loads.
     */
    static String tolerantMixinConfig(String json) {
        var root = JsonParser.parseString(json).getAsJsonObject();
        root.addProperty("required", false);
        var injectors = root.has("injectors") ? root.getAsJsonObject("injectors") : new com.google.gson.JsonObject();
        injectors.addProperty("defaultRequire", 0);
        root.add("injectors", injectors);
        return new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    /** The jar's Mixin config files (the manifest's MixinConfigs). */
    private static List<String> mixinConfigsOf(Path jar) throws IOException {
        List<String> out = new ArrayList<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry manifestEntry = zip.getEntry("META-INF/MANIFEST.MF");
            if (manifestEntry == null) return out;
            String configs;
            try (InputStream in = zip.getInputStream(manifestEntry)) {
                configs = new Manifest(in).getMainAttributes().getValue("MixinConfigs");
            }
            if (configs == null) return out;
            for (String config : configs.split(",")) {
                if (!config.isBlank()) out.add(config.trim());
            }
        }
        return out;
    }

    /** Refmap files named by the jar's Mixin configs. */
    private static List<String> refmapsOf(Path jar) throws IOException {
        List<String> out = new ArrayList<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (String config : mixinConfigsOf(jar)) {
                ZipEntry e = zip.getEntry(config);
                if (e == null) continue;
                try (InputStream in = zip.getInputStream(e)) {
                    var json = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    if (json.has("refmap")) out.add(json.get("refmap").getAsString());
                }
            }
        }
        return out;
    }

    private static boolean isSignature(String name) {
        return name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"));
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

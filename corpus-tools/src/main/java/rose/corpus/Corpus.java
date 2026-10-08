package rose.corpus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

/** Builds the local corpus: Mojang's game jars, mappings, Mojang-named jars and decompiled sources. */
final class Corpus {
    static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final Path root;
    private final Downloader downloader = new Downloader();
    private JsonObject manifest;

    Corpus(Path root) {
        this.root = root;
    }

    /** Downloads and verifies the version JSON, client jar, server jar and (if published) mappings. */
    MinecraftVersion fetch(String id) throws IOException, InterruptedException {
        MinecraftVersion mc = new MinecraftVersion(id, root.resolve("minecraft").resolve(id));
        Files.createDirectories(mc.dir());

        JsonObject entry = findInManifest(id);
        downloader.download(entry.get("url").getAsString(), mc.versionJson(), entry.get("sha1").getAsString());
        JsonObject version = JsonParser.parseString(Files.readString(mc.versionJson())).getAsJsonObject();
        JsonObject downloads = version.getAsJsonObject("downloads");

        download(downloads, "client", mc.clientJar());
        download(downloads, "server", mc.serverBundlerJar());
        download(downloads, "client_mappings", mc.clientMappings());
        download(downloads, "server_mappings", mc.serverMappings());
        extractServerFromBundler(mc);
        return mc;
    }

    /**
     * Produces {@link MinecraftVersion#namedClientJar()}. Obfuscated versions (before 26.1) are remapped
     * with Mojang's official mappings; unobfuscated versions already use real names and are copied.
     */
    void remapToMojangNames(MinecraftVersion mc) throws IOException {
        Path out = mc.namedClientJar();
        if (Files.exists(out)) {
            System.out.println("[corpus] cached  " + out);
            return;
        }
        if (!mc.isObfuscated()) {
            System.out.println("[corpus] " + mc.id() + " is unobfuscated; using the client jar as-is");
            Files.copy(mc.clientJar(), out);
            return;
        }

        System.out.println("[corpus] remap   " + mc.id() + " client -> Mojang names");
        // ProGuard files map named -> obfuscated ("source" -> "target"). Flip them so obfuscated is the source.
        MemoryMappingTree proguard = new MemoryMappingTree();
        MappingReader.read(mc.clientMappings(), MappingFormat.PROGUARD_FILE, proguard);
        MemoryMappingTree obfToNamed = new MemoryMappingTree();
        proguard.accept(new MappingSourceNsSwitch(obfToNamed, "target"));

        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(provider(obfToNamed))
                .renameInvalidLocals(true)
                .rebuildSourceFilenames(true)
                .build();
        Path tmp = out.resolveSibling(out.getFileName() + ".part");
        Files.deleteIfExists(tmp);
        try (OutputConsumerPath output = new OutputConsumerPath.Builder(tmp).build()) {
            output.addNonClassFiles(mc.clientJar(), NonClassCopyMode.FIX_META_INF, remapper);
            remapper.readInputs(mc.clientJar());
            remapper.apply(output);
        } finally {
            remapper.finish();
        }
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Decompiles the Mojang-named client jar into readable Java source with Vineflower. */
    void decompile(MinecraftVersion mc) throws IOException {
        if (!Files.exists(mc.namedClientJar())) remapToMojangNames(mc);
        Path marker = mc.sourceDir().resolve(".complete");
        if (Files.exists(marker)) {
            System.out.println("[corpus] cached  " + mc.sourceDir());
            return;
        }
        System.out.println("[corpus] decompile " + mc.id() + " (this takes several minutes)");
        Files.createDirectories(mc.sourceDir());
        ConsoleDecompiler.main(new String[] {
            "--decompile-generics=1",
            "--remove-synthetic=1",
            "--remove-bridge=1",
            "--log-level=warn",
            "--only=net/minecraft/",
            "--only=com/mojang/",
            mc.namedClientJar().toString(),
            mc.sourceDir().toString(),
        });
        Files.writeString(marker, mc.id());
    }

    private void download(JsonObject downloads, String key, Path target) throws IOException, InterruptedException {
        JsonObject file = downloads.getAsJsonObject(key);
        if (file == null) return; // e.g. no mappings for unobfuscated versions
        downloader.download(file.get("url").getAsString(), target, file.get("sha1").getAsString());
    }

    /** Since 1.18 the server download is a "bundler" jar; the real server jar sits inside it. */
    private static void extractServerFromBundler(MinecraftVersion mc) throws IOException {
        if (Files.exists(mc.serverJar())) return;
        try (FileSystem zip = FileSystems.newFileSystem(URI.create("jar:" + mc.serverBundlerJar().toUri()), Map.of())) {
            Path versionsList = zip.getPath("META-INF", "versions.list");
            if (!Files.exists(versionsList)) {
                Files.copy(mc.serverBundlerJar(), mc.serverJar()); // pre-1.18: not a bundler
                return;
            }
            // Each line: <sha256>\t<id>\t<path inside META-INF/versions/>
            String[] parts = Files.readAllLines(versionsList).getFirst().split("\t");
            Files.copy(zip.getPath("META-INF", "versions", parts[2]), mc.serverJar());
        }
    }

    private JsonObject findInManifest(String id) throws IOException, InterruptedException {
        if (manifest == null) {
            manifest = JsonParser.parseString(downloader.getString(VERSION_MANIFEST)).getAsJsonObject();
        }
        for (var element : manifest.getAsJsonArray("versions")) {
            JsonObject entry = element.getAsJsonObject();
            if (entry.get("id").getAsString().equals(id)) return entry;
        }
        throw new IOException("Minecraft version not found in Mojang's manifest: " + id);
    }

    /** Feeds a mapping-io tree (source namespace = obfuscated, dst 0 = named) into tiny-remapper. */
    private static IMappingProvider provider(MappingTree tree) {
        return out -> {
            for (MappingTree.ClassMapping cls : tree.getClasses()) {
                String owner = cls.getSrcName();
                String named = cls.getDstName(0);
                if (named != null) out.acceptClass(owner, named);
                for (MappingTree.MethodMapping method : cls.getMethods()) {
                    String dst = method.getDstName(0);
                    if (dst != null) {
                        out.acceptMethod(new IMappingProvider.Member(owner, method.getSrcName(), method.getSrcDesc()), dst);
                    }
                }
                for (MappingTree.FieldMapping field : cls.getFields()) {
                    String dst = field.getDstName(0);
                    if (dst != null) {
                        out.acceptField(new IMappingProvider.Member(owner, field.getSrcName(), field.getSrcDesc()), dst);
                    }
                }
            }
        };
    }
}

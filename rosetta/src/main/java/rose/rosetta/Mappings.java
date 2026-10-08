package rose.rosetta;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

/**
 * One mapping set flattened into lookup tables keyed by the <em>obfuscated</em> ("official") names of a Minecraft
 * version: classes by name, methods by {@code owner.name+desc}, fields by {@code owner.name}.
 */
final class Mappings {
    final Map<String, String> classes = new HashMap<>();
    final Map<String, String> methods = new HashMap<>();
    final Map<String, String> fields = new HashMap<>();
    /** Field descriptors by {@code owner.name}, where the format has them (intermediary). */
    final Map<String, String> fieldDescs = new HashMap<>();

    static String methodKey(String owner, String name, String desc) {
        return owner + "." + name + desc;
    }

    static String fieldKey(String owner, String name) {
        return owner + "." + name;
    }

    /** Fabric intermediary ({@code mappings/mappings.tiny} inside the jar): official -> intermediary. */
    static Mappings intermediary(Path jar) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        try (ZipFile zip = new ZipFile(jar.toFile());
             Reader reader = reader(zip.getInputStream(zip.getEntry("mappings/mappings.tiny")))) {
            MappingReader.read(reader, MappingFormat.TINY_2_FILE, tree);
        }
        return flatten(tree, "intermediary");
    }

    /** Mojang's official ProGuard mappings (named -> obfuscated), flipped to official -> Mojang names. */
    static Mappings mojang(Path proguardFile) throws IOException {
        MemoryMappingTree proguard = new MemoryMappingTree();
        MappingReader.read(proguardFile, MappingFormat.PROGUARD_FILE, proguard);
        MemoryMappingTree flipped = new MemoryMappingTree();
        proguard.accept(new MappingSourceNsSwitch(flipped, "target"));
        return flatten(flipped, "source");
    }

    /** MCPConfig's {@code config/joined.tsrg} (TSRG2: obf, srg, id): official -> SRG names. */
    static Mappings srg(Path mcpConfigZip) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        try (ZipFile zip = new ZipFile(mcpConfigZip.toFile());
             Reader reader = reader(zip.getInputStream(zip.getEntry("config/joined.tsrg")))) {
            MappingReader.read(reader, MappingFormat.TSRG_2_FILE, tree);
        }
        return flatten(tree, "srg");
    }

    private static Mappings flatten(MappingTree tree, String dstNamespace) {
        int ns = tree.getNamespaceId(dstNamespace);
        if (ns < 0) throw new IllegalStateException("mapping has no namespace '" + dstNamespace + "': " + tree.getDstNamespaces());
        Mappings out = new Mappings();
        for (MappingTree.ClassMapping cls : tree.getClasses()) {
            String owner = cls.getSrcName();
            String name = cls.getDstName(ns);
            if (name != null) out.classes.put(owner, name);
            for (MappingTree.MethodMapping m : cls.getMethods()) {
                String dst = m.getDstName(ns);
                if (dst != null && m.getSrcDesc() != null) out.methods.put(methodKey(owner, m.getSrcName(), m.getSrcDesc()), dst);
            }
            for (MappingTree.FieldMapping f : cls.getFields()) {
                String dst = f.getDstName(ns);
                if (dst != null) out.fields.put(fieldKey(owner, f.getSrcName()), dst);
                if (f.getSrcDesc() != null) out.fieldDescs.put(fieldKey(owner, f.getSrcName()), f.getSrcDesc());
            }
        }
        return out;
    }

    private static Reader reader(InputStream in) {
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }
}

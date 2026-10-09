package rose.rosetta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Old runtime names to Minecraft 26.3 names, for one source environment (e.g. Forge 1.20.1).
 *
 * <p>Classes are keyed by the old class name as it appears in mod bytecode. Methods and fields are keyed by their
 * old <em>runtime</em> name. For Forge 1.20.1 these are SRG names like {@code m_6227_}, which identify a whole
 * override family, so the owner isn't needed: a mod class that overrides {@code Block.use} uses the same SRG name.
 *
 * <p>File format (UTF-8, tab-separated, {@code #} comments):
 * <pre>
 * c  oldClass  newClass  how
 * m  oldName   readableOldName  newName  how
 * f  oldName   readableOldName  newName  how
 * r  owner.oldName  newName       (member renamed by name in a 26.3 class, for names that are not SRG names)
 * </pre>
 * {@code newName} is {@code -} when the member no longer exists. {@code how} records where the answer came from.
 */
public final class NameLayer {
    /** Where a mapping came from. */
    public enum How {
        /** Same name, still present in 26.3. */
        SAME,
        /** Renamed; followed through Fabric intermediary (identity across 1.14 - 1.21.11). */
        INTERMEDIARY,
        /** Intermediary lost it, but 26.3 declares the same readable name with an identical signature in the same class. */
        NAME_MATCH,
        /** From a hand-written, reviewed rule in rosetta/rules/. */
        RULE,
        /** Unique same-simple-name class in 26.3; likely a package move. Needs review. */
        HEURISTIC,
        /** Existed in the source version but has no counterpart in 26.3 (removed, split or merged). */
        GONE
    }

    public record ClassEntry(String oldName, String newName, How how) {}

    public record MemberEntry(String oldName, String readableOldName, String newName, How how) {
        public boolean exists() {
            return how != How.GONE;
        }
    }

    private final String source;
    private final Map<String, ClassEntry> classes;
    private final Map<String, MemberEntry> methods;
    private final Map<String, MemberEntry> fields;
    private final Map<String, String> memberRenames;

    public NameLayer(String source, Map<String, ClassEntry> classes, Map<String, MemberEntry> methods,
                     Map<String, MemberEntry> fields) {
        this(source, classes, methods, fields, Map.of());
    }

    /** @param memberRenames {@code owner26.oldName -> newName} for members kept by name in old bytecode (enum constants) */
    public NameLayer(String source, Map<String, ClassEntry> classes, Map<String, MemberEntry> methods,
                     Map<String, MemberEntry> fields, Map<String, String> memberRenames) {
        this.source = source;
        this.classes = Collections.unmodifiableMap(new TreeMap<>(classes));
        this.methods = Collections.unmodifiableMap(new TreeMap<>(methods));
        this.fields = Collections.unmodifiableMap(new TreeMap<>(fields));
        this.memberRenames = Collections.unmodifiableMap(new TreeMap<>(memberRenames));
    }

    /** The 26.3 name of member {@code name} of 26.3 class {@code owner}, if it was renamed by name; else {@code null}. */
    public String renamedMember(String owner, String name) {
        return memberRenames.get(owner + "." + name);
    }

    public Map<String, String> memberRenames() { return memberRenames; }

    public String source() { return source; }

    public Map<String, ClassEntry> classes() { return classes; }

    public Map<String, MemberEntry> methods() { return methods; }

    public Map<String, MemberEntry> fields() { return fields; }

    /** The class's 26.3 name, or the same name if the layer doesn't know it (mod, library or JDK classes). */
    public String mapClass(String internalName) {
        ClassEntry entry = classes.get(internalName);
        if (entry != null) return entry.newName();
        // Nested classes of a renamed outer class follow it: Outer$Inner -> NewOuter$Inner.
        int dollar = internalName.lastIndexOf('$');
        if (dollar > 0) {
            String outer = internalName.substring(0, dollar);
            String mappedOuter = mapClass(outer);
            if (!mappedOuter.equals(outer)) return mappedOuter + internalName.substring(dollar);
        }
        return internalName;
    }

    public ClassEntry classEntry(String internalName) {
        return classes.get(internalName);
    }

    public MemberEntry method(String oldName) {
        return methods.get(oldName);
    }

    public MemberEntry field(String oldName) {
        return fields.get(oldName);
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (BufferedWriter out = Files.newBufferedWriter(file)) {
            out.write("# Rose name layer v1\tsource=" + source + "\ttarget=26.3\n");
            out.write("# Generated from mapping files in corpus/; do not commit.\n");
            for (ClassEntry c : classes.values()) {
                out.write(String.join("\t", "c", c.oldName(), c.newName(), c.how().name()) + "\n");
            }
            for (MemberEntry m : methods.values()) {
                out.write(String.join("\t", "m", m.oldName(), m.readableOldName(), m.newName(), m.how().name()) + "\n");
            }
            for (MemberEntry f : fields.values()) {
                out.write(String.join("\t", "f", f.oldName(), f.readableOldName(), f.newName(), f.how().name()) + "\n");
            }
            for (var r : memberRenames.entrySet()) {
                out.write(String.join("\t", "r", r.getKey(), r.getValue()) + "\n");
            }
        }
    }

    public static NameLayer read(Path file) throws IOException {
        String source = "unknown";
        Map<String, ClassEntry> classes = new LinkedHashMap<>();
        Map<String, MemberEntry> methods = new LinkedHashMap<>();
        Map<String, MemberEntry> fields = new LinkedHashMap<>();
        Map<String, String> renames = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file)) {
            if (line.startsWith("# Rose name layer")) {
                for (String part : line.split("\t")) if (part.startsWith("source=")) source = part.substring(7);
                continue;
            }
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            switch (p[0]) {
                case "c" -> classes.put(p[1], new ClassEntry(p[1], p[2], How.valueOf(p[3])));
                case "m" -> methods.put(p[1], new MemberEntry(p[1], p[2], p[3], How.valueOf(p[4])));
                case "f" -> fields.put(p[1], new MemberEntry(p[1], p[2], p[3], How.valueOf(p[4])));
                case "r" -> renames.put(p[1], p[2]);
                default -> throw new IOException(file + ": unknown line kind '" + p[0] + "'");
            }
        }
        return new NameLayer(source, classes, methods, fields, renames);
    }
}

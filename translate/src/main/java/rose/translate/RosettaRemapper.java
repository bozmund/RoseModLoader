package rose.translate;

import java.util.regex.Pattern;
import org.objectweb.asm.commons.Remapper;
import rose.rosetta.NameLayer;

/**
 * Renames classes, methods and fields from a source environment's runtime names to 26.3 names.
 *
 * <p>Members are only renamed when they carry an obfuscation-tool name (SRG {@code m_123_}/{@code f_123_} for
 * Forge). Those names identify a whole override family, so overriding methods in mod classes are renamed with the
 * method they override, without needing the class hierarchy. Members that no longer exist in 26.3 get their
 * readable 1.20.1 name: era-bridge classes that re-create removed classes declare those names, and crash reports
 * stay readable. (The analyzer reports such members from the name layer, not from this renaming.)
 */
public final class RosettaRemapper extends Remapper {
    public static final Pattern SRG_METHOD = Pattern.compile("m_\\d+_");
    public static final Pattern SRG_FIELD = Pattern.compile("f_\\d+_");

    private final NameLayer layer;

    public RosettaRemapper(NameLayer layer) {
        this.layer = layer;
    }

    @Override
    public String map(String internalName) {
        NameLayer.ClassEntry entry = layer.classEntry(internalName);
        if (entry != null && !entry.how().equals(NameLayer.How.GONE)) return entry.newName();
        if (entry != null) return internalName; // gone: keep, so it gets reported
        return layer.mapClass(internalName);
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        return SRG_METHOD.matcher(name).matches() ? member(layer.method(name), name) : byOwner(owner, name);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return SRG_FIELD.matcher(name).matches() ? member(layer.field(name), name) : byOwner(owner, name);
    }

    /** Names that aren't SRG names (enum constants keep theirs) can still have been renamed in their 26.3 class. */
    private String byOwner(String owner, String name) {
        String renamed = layer.renamedMember(map(owner), name);
        return renamed != null ? renamed : name;
    }

    @Override
    public String mapInvokeDynamicMethodName(String name, String descriptor) {
        // Lambda factories name the functional interface method; it may be an SRG name too.
        return SRG_METHOD.matcher(name).matches() ? member(layer.method(name), name) : name;
    }

    /**
     * String constants that are exactly an SRG name are reflection targets ({@code ObfuscationReflectionHelper},
     * access transformers in code); they name the same member as in bytecode, so they get the same new name.
     */
    @Override
    public Object mapValue(Object value) {
        if (value instanceof String s) {
            if (SRG_METHOD.matcher(s).matches()) return member(layer.method(s), s);
            if (SRG_FIELD.matcher(s).matches()) return member(layer.field(s), s);
        }
        return super.mapValue(value);
    }

    private static String member(NameLayer.MemberEntry entry, String oldName) {
        if (entry == null) return oldName;
        return entry.exists() ? entry.newName() : entry.readableOldName();
    }
}

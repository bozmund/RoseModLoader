package rose.translate.forge;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import rose.translate.ClassIndex;
import rose.translate.RosettaRemapper;

/**
 * Turns a Forge access transformer ({@code META-INF/accesstransformer.cfg}, SRG names) into a Rose access widener
 * for 26.3, so translated code may touch the members the mod opened up. Only widening carries over: {@code public}
 * and {@code protected} become {@code accessible}, {@code -f} becomes {@code extendable} (classes, methods) or
 * {@code mutable} (fields). Entries for members gone in 26.3, or for classes that aren't the game's, are reported
 * and skipped.
 */
public final class AccessTransformerConverter {
    public static final String SOURCE = "META-INF/accesstransformer.cfg";
    public static final String TARGET = "META-INF/rose.accesswidener";

    private final RosettaRemapper remapper;
    private final ClassIndex game;

    public AccessTransformerConverter(RosettaRemapper remapper, ClassIndex game) {
        this.remapper = remapper;
        this.game = game;
    }

    /** The access widener text, or {@code null} if nothing carried over. Skipped lines are added to {@code report}. */
    public String convert(String accessTransformer, List<String> report) {
        Set<String> out = new LinkedHashSet<>();
        for (String raw : accessTransformer.split("\\R")) {
            String line = raw.replaceAll("#.*", "").strip();
            if (line.isEmpty()) continue;
            String[] p = line.split("\\s+");
            String skipped = p.length < 2 || p.length > 3 ? "can't parse" : convert(p, out);
            if (skipped != null) report.add("access transformer '" + line + "' skipped: " + skipped);
        }
        if (out.isEmpty()) return null;
        return "accessWidener v2 named\n" + String.join("\n", out) + "\n";
    }

    private String convert(String[] p, Set<String> out) {
        String modifier = p[0];
        boolean definal = modifier.endsWith("-f");
        String access = modifier.replaceAll("[-+]f$", "");
        boolean widen = access.equals("public") || access.equals("protected");
        String owner = remapper.map(p[1].replace('.', '/'));
        ClassIndex.Info info = game.get(owner);
        if (info == null) return "no class " + owner + " in 26.3";
        List<String> entries = new ArrayList<>();
        if (p.length == 2) {
            if (widen) entries.add("accessible class " + owner);
            if (definal) entries.add("extendable class " + owner);
        } else if (p[2].contains("(")) {
            int paren = p[2].indexOf('(');
            String oldName = p[2].substring(0, paren);
            String name = remapper.mapMethodName(p[1].replace('.', '/'), oldName, p[2].substring(paren));
            String desc = remapper.mapMethodDesc(p[2].substring(paren));
            if (!info.methods().contains(name + desc)) return "no method " + owner + "." + name + desc + " in 26.3";
            if (widen) entries.add("accessible method " + owner + " " + name + " " + desc);
            if (definal) entries.add("extendable method " + owner + " " + name + " " + desc);
        } else {
            String name = remapper.mapFieldName(p[1].replace('.', '/'), p[2], null);
            String desc = info.fields().stream().filter(f -> f.startsWith(name + ":")).findFirst()
                    .map(f -> f.substring(name.length() + 1)).orElse(null);
            if (desc == null) return "no field " + owner + "." + name + " in 26.3";
            if (widen) entries.add("accessible field " + owner + " " + name + " " + desc);
            if (definal) entries.add("mutable field " + owner + " " + name + " " + desc);
        }
        if (entries.isEmpty()) return "only narrows access";
        out.addAll(entries);
        return null;
    }
}

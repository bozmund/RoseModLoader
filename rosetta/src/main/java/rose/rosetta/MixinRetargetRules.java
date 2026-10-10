package rose.rosetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mixin retargets: a vanilla method an old Mixin targets was renamed or re-signatured, and the new method does the
 * same job (the rebaser moves the Mixin's selectors there and adapts its handlers; see MixinRebaser).
 *
 * <p>File format ({@code rosetta/rules/<source>/mixin-retargets.tsv}, tab-separated, {@code #} comments):
 * <pre>
 * old                                                     new                                                  evidence
 * Lnet/minecraft/world/level/block/DispenserBlock;dispenseFrom(L...ServerLevel;L...BlockPos;)V  Lnet/...;dispenseFrom(L...ServerLevel;L...BlockState;L...BlockPos;)V  1.20.2: ...
 * </pre>
 * Both are Mixin selectors ({@code Lowner;name(desc)ret}) in translated names: after Rosetta's renames, as the
 * translated Mixin's refmap shows them.
 */
public final class MixinRetargetRules {
    public record Retarget(String oldSelector, String newSelector, String evidence) {
        public String oldDescriptor() {
            return oldSelector.substring(oldSelector.indexOf('('));
        }

        public String newDescriptor() {
            return newSelector.substring(newSelector.indexOf('('));
        }

        public String newName() {
            return newSelector.substring(newSelector.indexOf(';') + 1, newSelector.indexOf('('));
        }
    }

    private final Map<String, Retarget> byOld;

    public MixinRetargetRules(Map<String, Retarget> byOld) {
        this.byOld = Map.copyOf(byOld);
    }

    public static MixinRetargetRules empty() {
        return new MixinRetargetRules(Map.of());
    }

    public static MixinRetargetRules read(Path file) throws IOException {
        if (!Files.exists(file)) return empty();
        Map<String, Retarget> rules = new LinkedHashMap<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 3 || p[2].isBlank()) {
                throw new IOException(file + ":" + lineNo + ": expected old<TAB>new<TAB>evidence (evidence is required)");
            }
            for (String selector : new String[] {p[0], p[1]}) {
                if (!selector.matches("L[^;]+;[^(]+\\(.*\\).+")) throw new IOException(file + ":" + lineNo + ": bad selector " + selector);
            }
            if (!p[0].substring(0, p[0].indexOf(';')).equals(p[1].substring(0, p[1].indexOf(';')))) {
                throw new IOException(file + ":" + lineNo + ": a retarget stays in its class: " + p[0] + " -> " + p[1]);
            }
            if (rules.putIfAbsent(p[0], new Retarget(p[0], p[1], p[2])) != null) throw new IOException(file + ":" + lineNo + ": duplicate rule for " + p[0]);
        }
        return new MixinRetargetRules(rules);
    }

    /** The retarget for a selector ({@code Lowner;name(desc)ret}), or {@code null}. */
    public Retarget find(String selector) {
        return byOld.get(selector);
    }

    /** The retarget of a method {@code name+desc} in {@code owner} (internal name), or {@code null}. */
    public Retarget find(String owner, String name, String desc) {
        return byOld.get("L" + owner + ";" + name + desc);
    }

    public boolean isEmpty() {
        return byOld.isEmpty();
    }
}

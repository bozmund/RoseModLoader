package rose.rosetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Inheritance bridges: methods 26.3 calls on a vanilla type that old mod subclasses don't have, because the method
 * is new or its signature changed. The translator adds the method to the mod class; its body calls a static era
 * helper that runs the mod's old method and converts arguments and results.
 *
 * <p>File format ({@code rosetta/rules/<source>/bridges.tsv}, tab-separated, {@code #} comments), all in 26.3 names:
 * <pre>
 * target   method(desc)   when(desc) or -   helperOwner.helperName   evidence
 * </pre>
 * <ul>
 *   <li>{@code target}: a vanilla class or interface; mod classes that extend or implement it get the bridge.</li>
 *   <li>{@code when}: the bridge is added only to mod classes that declare this (translated, old) method, i.e. that
 *       overrode the old version. {@code -}: added to every concrete mod class that doesn't have the method.</li>
 *   <li>The helper is {@code static R name(Target self, <method args>)}.</li>
 * </ul>
 */
public record BridgeRules(List<Bridge> bridges) {
    public record Bridge(String target, String method, String when, String helperOwner, String helperName, String evidence) {
        public String methodName() {
            return method.substring(0, method.indexOf('('));
        }

        public String methodDesc() {
            return method.substring(method.indexOf('('));
        }

        public boolean always() {
            return when.equals("-");
        }

        /** {@code (LTarget;<args>)R}. */
        public String helperDescriptor() {
            return "(L" + target + ";" + methodDesc().substring(1);
        }
    }

    public static BridgeRules empty() {
        return new BridgeRules(List.of());
    }

    public static BridgeRules read(Path file) throws IOException {
        if (!Files.exists(file)) return empty();
        List<Bridge> out = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 5 || p[4].isBlank()) {
                throw new IOException(file + ":" + lineNo + ": expected target<TAB>method<TAB>when<TAB>helper<TAB>evidence (evidence is required)");
            }
            if (!p[1].contains("(") || (!p[2].equals("-") && !p[2].contains("("))) throw new IOException(file + ":" + lineNo + ": bad method");
            int dot = p[3].lastIndexOf('.');
            if (dot <= 0) throw new IOException(file + ":" + lineNo + ": helper must be owner/Class.method, got " + p[3]);
            out.add(new Bridge(p[0], p[1], p[2], p[3].substring(0, dot), p[3].substring(dot + 1), p[4]));
        }
        return new BridgeRules(List.copyOf(out));
    }
}

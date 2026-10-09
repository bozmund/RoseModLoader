package rose.rosetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Value conversions the call adapter may insert when an API changed a parameter, return or field type in a way
 * that keeps the meaning: 1.20.5 started passing registry {@code Holder}s where 1.20.1 passed the value
 * ({@code MobEffect} to {@code Holder<MobEffect>}).
 *
 * <p>File format ({@code rosetta/rules/<source>/conversions.tsv}, 26.3 names):
 * <pre>
 * from   to   helperOwner.helperName   evidence
 * </pre>
 * The helper is {@code static To name(From value)}. {@code to} may be {@code *} for a conversion that works for any
 * target type (the result is then cast), such as {@code Holder -> *} via {@code holder.value()}.
 */
public record ConversionRules(Map<String, Conversion> byPair) {
    public record Conversion(String from, String to, String helperOwner, String helperName, String evidence) {
        public String helperDescriptor() {
            String result = to.equals("*") ? "java/lang/Object" : to;
            return "(L" + from + ";)L" + result + ";";
        }

        public boolean anyTarget() {
            return to.equals("*");
        }
    }

    public static ConversionRules empty() {
        return new ConversionRules(Map.of());
    }

    /** The conversion from {@code from} to {@code to}, or {@code null}. */
    public Conversion find(String from, String to) {
        Conversion exact = byPair.get(from + "->" + to);
        return exact != null ? exact : byPair.get(from + "->*");
    }

    public boolean isEmpty() {
        return byPair.isEmpty();
    }

    public static ConversionRules read(Path file) throws IOException {
        if (!Files.exists(file)) return empty();
        Map<String, Conversion> out = new LinkedHashMap<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 4 || p[3].isBlank()) throw new IOException(file + ":" + lineNo + ": expected from<TAB>to<TAB>helper<TAB>evidence");
            int dot = p[2].lastIndexOf('.');
            if (dot <= 0) throw new IOException(file + ":" + lineNo + ": helper must be owner/Class.method");
            Conversion c = new Conversion(p[0], p[1], p[2].substring(0, dot), p[2].substring(dot + 1), p[3]);
            if (out.putIfAbsent(p[0] + "->" + p[1], c) != null) throw new IOException(file + ":" + lineNo + ": duplicate conversion");
        }
        return new ConversionRules(Map.copyOf(out));
    }
}

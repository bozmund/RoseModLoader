package rose.rosetta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Hand-written class renames ({@code old<TAB>new<TAB>evidence}), from {@code rosetta/rules/*.tsv}. */
public record RenameRules(Map<String, String> renames) {
    public static RenameRules read(Path file) throws IOException {
        Map<String, String> renames = new LinkedHashMap<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file)) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split("\t");
            if (p.length < 3 || p[2].isBlank()) {
                throw new IOException(file + ":" + lineNo + ": expected old<TAB>new<TAB>evidence (evidence is required)");
            }
            if (renames.putIfAbsent(p[0], p[1]) != null) throw new IOException(file + ":" + lineNo + ": duplicate rule for " + p[0]);
        }
        return new RenameRules(Map.copyOf(renames));
    }

    public String apply(String name) {
        return renames.get(name);
    }
}

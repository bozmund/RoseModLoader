package rose.testing.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Differences the native port made on purpose, so they don't count against Rose. One per line:
 * {@code <pattern><TAB><reason>}. The pattern matches {@link Difference#id()} ({@code file key path}); {@code *}
 * matches anything. Lines starting with {@code #} are comments. Every entry needs a reason: what the port changed and
 * where that shows (a changelog line, a commit, a source file).
 */
public final class AllowList {
    public record Entry(String pattern, String reason, Pattern regex) {}

    private final List<Entry> entries;

    public AllowList(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static AllowList empty() {
        return new AllowList(List.of());
    }

    public static AllowList read(Path file) throws IOException {
        return parse(Files.readAllLines(file));
    }

    public static AllowList parse(List<String> lines) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] parts = line.split("\t", 2);
            if (parts.length < 2 || parts[1].isBlank()) {
                throw new IllegalArgumentException("allow-list line " + (i + 1) + " needs <pattern><TAB><reason>: " + line);
            }
            entries.add(new Entry(parts[0], parts[1].strip(), glob(parts[0])));
        }
        return new AllowList(entries);
    }

    private static Pattern glob(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (String part : pattern.split("\\*", -1)) {
            if (!regex.isEmpty()) regex.append(".*");
            regex.append(Pattern.quote(part));
        }
        return Pattern.compile(regex.toString());
    }

    public Optional<Entry> match(Difference difference) {
        String id = difference.id();
        return entries.stream().filter(e -> e.regex().matcher(id).matches()).findFirst();
    }

    public List<Entry> entries() {
        return entries;
    }
}

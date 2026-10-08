package rose.foundry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Small, robust-enough extraction of method declarations and usage lines from decompiled Java source. */
final class SourceSnippets {
    private static final Pattern DECLARATION_START = Pattern.compile(
            "^\\s*(public|protected|private|static|final|abstract|synchronized|default|native|<).*");

    /** The full declarations (signature + body) of methods named {@code name} in a source file. */
    static List<String> methods(Path source, String name, int maxMethods, int maxLines) throws IOException {
        List<String> out = new ArrayList<>();
        if (!Files.exists(source)) return out;
        List<String> lines = Files.readAllLines(source);
        Pattern call = Pattern.compile("[\\s.]" + Pattern.quote(name) + "\\s*\\(");
        for (int i = 0; i < lines.size() && out.size() < maxMethods; i++) {
            String line = lines.get(i);
            if (!DECLARATION_START.matcher(line).matches() || !call.matcher(" " + line).find() || line.contains("=")) continue;
            if (line.trim().startsWith("return") || line.contains(" new ")) continue;
            out.add(block(lines, i, maxLines));
        }
        return out;
    }

    /** Method signature lines of a class (public/protected), to show what exists when nothing matches by name. */
    static List<String> signatures(Path source, int max) throws IOException {
        List<String> out = new ArrayList<>();
        if (!Files.exists(source)) return out;
        for (String line : Files.readAllLines(source)) {
            String t = line.trim();
            if ((t.startsWith("public ") || t.startsWith("protected ")) && t.contains("(") && !t.contains("=")
                    && !t.contains(" class ") && !t.contains(" record ") && !t.contains(" interface ")) {
                out.add(t.endsWith("{") ? t.substring(0, t.length() - 1).trim() : t);
                if (out.size() >= max) break;
            }
        }
        return out;
    }

    /** Lines that call {@code name(}, with a few lines of context, formatted with line numbers. */
    static List<String> usages(Path source, String name, int context, int maxHits) throws IOException {
        List<String> out = new ArrayList<>();
        if (!Files.exists(source)) return out;
        List<String> lines = Files.readAllLines(source);
        Pattern call = Pattern.compile("\\." + Pattern.quote(name) + "\\s*\\(");
        for (int i = 0; i < lines.size() && out.size() < maxHits; i++) {
            if (!call.matcher(lines.get(i)).find()) continue;
            StringBuilder s = new StringBuilder();
            for (int j = Math.max(0, i - context); j <= Math.min(lines.size() - 1, i + context); j++) {
                s.append(String.format("%5d%s %s%n", j + 1, j == i ? ">" : " ", lines.get(j)));
            }
            out.add(s.toString());
        }
        return out;
    }

    private static String block(List<String> lines, int start, int maxLines) {
        StringBuilder out = new StringBuilder();
        int depth = 0;
        boolean opened = false;
        for (int i = start; i < lines.size() && i < start + maxLines; i++) {
            String line = lines.get(i);
            out.append(line).append('\n');
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    depth++;
                    opened = true;
                } else if (c == '}') {
                    depth--;
                }
            }
            if ((opened && depth <= 0) || (!opened && line.trim().endsWith(";"))) return out.toString();
        }
        return out.append("   ... (truncated)\n").toString();
    }

    private SourceSnippets() {}
}

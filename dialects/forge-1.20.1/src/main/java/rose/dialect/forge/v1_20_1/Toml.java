package rose.dialect.forge.v1_20_1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The subset of TOML that Forge config files use: {@code [table.headers]}, {@code key = value} with booleans,
 * integers, floats, basic strings and (possibly multi-line) arrays of those, and {@code #} comments.
 */
public final class Toml {
    /** Dotted path ({@code table.key}) to Boolean, Long, Double, String or List. Malformed lines are skipped. */
    public static Map<String, Object> read(String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        String table = "";
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = stripComment(lines[i]).strip();
            if (line.isEmpty()) continue;
            if (line.startsWith("[") && line.endsWith("]") && !line.startsWith("[[")) {
                table = line.substring(1, line.length() - 1).strip();
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            String key = unquoteKey(line.substring(0, eq).strip());
            StringBuilder value = new StringBuilder(line.substring(eq + 1).strip());
            // Arrays may span lines.
            while (value.length() > 0 && value.charAt(0) == '[' && depth(value) > 0 && i + 1 < lines.length) {
                value.append(' ').append(stripComment(lines[++i]).strip());
            }
            Object parsed = new Parser(value.toString()).value();
            if (parsed != null) out.put(table.isEmpty() ? key : table + "." + key, parsed);
        }
        return out;
    }

    public static String format(Object value) {
        if (value instanceof String s) return quote(s);
        if (value instanceof Enum<?> e) return quote(e.name());
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object o : list) parts.add(format(o));
            return "[" + String.join(", ", parts) + "]";
        }
        return String.valueOf(value);
    }

    public static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static String unquoteKey(String key) {
        return key.length() >= 2 && key.startsWith("\"") && key.endsWith("\"") ? key.substring(1, key.length() - 1) : key;
    }

    private static int depth(CharSequence s) {
        int depth = 0;
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inString = !inString;
            else if (!inString && c == '[') depth++;
            else if (!inString && c == ']') depth--;
        }
        return depth;
    }

    private static String stripComment(String line) {
        boolean inString = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) inString = !inString;
            else if (c == '#' && !inString) return line.substring(0, i);
        }
        return line;
    }

    private static final class Parser {
        private final String s;
        private int pos;

        Parser(String s) {
            this.s = s;
        }

        Object value() {
            skipSpace();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            if (c == '"' || c == '\'') return string(c);
            if (c == '[') return array();
            int start = pos;
            while (pos < s.length() && ",] \t".indexOf(s.charAt(pos)) < 0) pos++;
            String token = s.substring(start, pos).replace("_", "");
            if (token.equals("true")) return Boolean.TRUE;
            if (token.equals("false")) return Boolean.FALSE;
            try {
                // Not a ternary: it would promote the long to double.
                if (token.matches("[+-]?\\d+")) return Long.parseLong(token);
                return Double.parseDouble(token);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private String string(char quote) {
            StringBuilder out = new StringBuilder();
            pos++;
            while (pos < s.length() && s.charAt(pos) != quote) {
                char c = s.charAt(pos++);
                if (c == '\\' && quote == '"' && pos < s.length()) {
                    char e = s.charAt(pos++);
                    out.append(switch (e) {
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        default -> e;
                    });
                } else {
                    out.append(c);
                }
            }
            pos++;
            return out.toString();
        }

        private List<Object> array() {
            List<Object> out = new ArrayList<>();
            pos++;
            while (true) {
                skipSpace();
                if (pos >= s.length()) return out;
                if (s.charAt(pos) == ']') {
                    pos++;
                    return out;
                }
                if (s.charAt(pos) == ',') {
                    pos++;
                    continue;
                }
                Object v = value();
                if (v == null) return out;
                out.add(v);
            }
        }

        private void skipSpace() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }
    }

    private Toml() {}
}

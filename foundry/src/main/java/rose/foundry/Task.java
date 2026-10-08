package rose.foundry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One unit of foundry work, stored as Markdown with a simple {@code key: value} header:
 *
 * <pre>
 * ---
 * id: farmersdelight-1a2b3c4d
 * kind: redirect
 * tier: S
 * ...
 * ---
 * (human-readable description)
 * </pre>
 */
public final class Task {
    public enum State { OPEN, CLAIMED, LANDED, ESCALATED, REJECTED;

        String folder() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final Map<String, String> fields = new LinkedHashMap<>();
    private String body;
    private State state;

    Task(State state, Map<String, String> fields, String body) {
        this.state = state;
        this.fields.putAll(fields);
        this.body = body;
    }

    public String id() { return get("id"); }

    public String kind() { return get("kind"); }

    public String tier() { return get("tier"); }

    public State state() { return state; }

    void state(State state) { this.state = state; }

    public String get(String key) {
        return fields.get(key);
    }

    public int getInt(String key, int fallback) {
        String v = fields.get(key);
        return v == null ? fallback : Integer.parseInt(v.trim());
    }

    public void set(String key, Object value) {
        fields.put(key, String.valueOf(value));
    }

    public String body() { return body; }

    public void appendBody(String text) {
        body = body + text;
    }

    String serialize() {
        StringBuilder out = new StringBuilder("---\n");
        fields.forEach((k, v) -> out.append(k).append(": ").append(v.replace("\n", " ")).append('\n'));
        return out.append("---\n").append(body).toString();
    }

    static Task parse(State state, String text) {
        Map<String, String> fields = new LinkedHashMap<>();
        String body = text;
        if (text.startsWith("---\n")) {
            int end = text.indexOf("\n---\n", 4);
            if (end > 0) {
                for (String line : text.substring(4, end).split("\n")) {
                    int colon = line.indexOf(':');
                    if (colon > 0) fields.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
                }
                body = text.substring(end + 5);
            }
        }
        return new Task(state, fields, body);
    }
}

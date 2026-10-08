package rose.bridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Recent game events (log lines, chat, overlay messages, joins, ...) with increasing sequence numbers, so agents
 * can poll "everything after N" without missing or repeating anything.
 */
public final class EventLog {
    private static final int CAPACITY = 5000;
    private static final Deque<JsonObject> EVENTS = new ArrayDeque<>();
    private static long nextSeq = 1;

    public static synchronized void add(String type, JsonObject data) {
        JsonObject event = new JsonObject();
        event.addProperty("seq", nextSeq++);
        event.addProperty("time", System.currentTimeMillis());
        event.addProperty("type", type);
        event.add("data", data);
        EVENTS.addLast(event);
        if (EVENTS.size() > CAPACITY) EVENTS.removeFirst();
    }

    public static void add(String type, String key, String value) {
        JsonObject data = new JsonObject();
        data.addProperty(key, value);
        add(type, data);
    }

    /**
     * @param after only events with a larger seq
     * @param types only these types (empty = all)
     */
    public static synchronized JsonObject poll(long after, Set<String> types, int limit) {
        JsonArray out = new JsonArray();
        for (JsonObject event : EVENTS) {
            if (out.size() >= limit) break;
            if (event.get("seq").getAsLong() <= after) continue;
            if (!types.isEmpty() && !types.contains(event.get("type").getAsString())) continue;
            out.add(event);
        }
        JsonObject result = new JsonObject();
        result.add("events", out);
        result.addProperty("lastSeq", nextSeq - 1);
        return result;
    }

    private EventLog() {}
}

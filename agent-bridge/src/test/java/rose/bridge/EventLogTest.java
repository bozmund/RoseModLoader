package rose.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventLogTest {
    @Test
    void pollReturnsOnlyNewerEventsOfRequestedTypes() {
        long start = EventLog.poll(0, Set.of(), Integer.MAX_VALUE).get("lastSeq").getAsLong();
        EventLog.add("chat", "text", "hello");
        EventLog.add("overlay", "text", "Counter: 1");
        EventLog.add("chat", "text", "bye");

        JsonObject chats = EventLog.poll(start, Set.of("chat"), 100);
        assertEquals(2, chats.getAsJsonArray("events").size());
        assertEquals("hello", chats.getAsJsonArray("events").get(0).getAsJsonObject()
                .getAsJsonObject("data").get("text").getAsString());

        long last = chats.get("lastSeq").getAsLong();
        assertEquals(start + 3, last);
        assertTrue(EventLog.poll(last, Set.of(), 100).getAsJsonArray("events").isEmpty());
    }

    @Test
    void limitCapsResults() {
        long start = EventLog.poll(0, Set.of(), Integer.MAX_VALUE).get("lastSeq").getAsLong();
        for (int i = 0; i < 5; i++) EventLog.add("log", "message", "line " + i);
        assertEquals(2, EventLog.poll(start, Set.of(), 2).getAsJsonArray("events").size());
    }
}

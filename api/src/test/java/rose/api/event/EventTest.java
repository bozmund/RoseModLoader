package rose.api.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class EventTest {
    @Test
    void invokerWithNoListenersDoesNothing() {
        Event<Consumer<String>> event = Event.ofConsumer();
        event.invoker().accept("ignored");
    }

    @Test
    void listenersRunInRegistrationOrder() {
        Event<Consumer<String>> event = Event.ofConsumer();
        List<String> calls = new ArrayList<>();
        event.register(v -> calls.add("a:" + v));
        event.register(v -> calls.add("b:" + v));

        event.invoker().accept("x");

        assertEquals(List.of("a:x", "b:x"), calls);
    }

    @Test
    void rejectsNullListener() {
        Event<Consumer<String>> event = Event.ofConsumer();
        assertThrows(NullPointerException.class, () -> event.register(null));
    }
}

package rose.api.event;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A hook mods can listen to. Listeners run in registration order on the thread that fires the event.
 *
 * <p>Firing goes through {@link #invoker()}, which is rebuilt on each registration, so firing stays a plain loop
 * over an array. Same approach as Fabric API's events.
 *
 * @param <T> the listener type, usually a functional interface
 */
public final class Event<T> {
    private final Function<T[], T> combiner;
    private T[] listeners;
    private volatile T invoker;

    private Event(T[] empty, Function<T[], T> combiner) {
        this.combiner = combiner;
        this.listeners = empty;
        this.invoker = combiner.apply(empty);
    }

    /**
     * @param empty    an empty array of the listener type
     * @param combiner builds one listener that calls all of the given listeners
     */
    public static <T> Event<T> create(T[] empty, Function<T[], T> combiner) {
        return new Event<>(empty, combiner);
    }

    /** Convenience for events whose listeners take a single argument. */
    @SuppressWarnings("unchecked")
    public static <A> Event<Consumer<A>> ofConsumer() {
        return create((Consumer<A>[]) new Consumer[0], listeners -> value -> {
            for (Consumer<A> listener : listeners) listener.accept(value);
        });
    }

    public synchronized void register(T listener) {
        if (listener == null) throw new NullPointerException("listener");
        listeners = Arrays.copyOf(listeners, listeners.length + 1);
        listeners[listeners.length - 1] = listener;
        invoker = combiner.apply(listeners);
    }

    /** Calls every listener. Used by Rose core (and mods that define their own events). */
    public T invoker() {
        return invoker;
    }
}

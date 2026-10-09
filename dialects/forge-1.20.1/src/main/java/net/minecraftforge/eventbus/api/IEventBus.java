package net.minecraftforge.eventbus.api;

import java.util.function.Consumer;

/**
 * An event bus. Forge 1.20.1 found a lambda listener's event type with TypeTools; on Rose the translator passes it
 * explicitly ({@code rose$addListener}; see {@code rose.translate.forge.TypedListenerTransform}).
 */
public interface IEventBus {
    /** Registers {@link SubscribeEvent} methods: the static ones of a {@code Class}, the instance ones of an object. */
    void register(Object target);

    <T extends Event> void addListener(Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer);

    void unregister(Object object);

    /** Returns whether the event was canceled. */
    boolean post(Event event);

    void start();

    // Rose: typed forms of the lambda overloads above; translated bytecode calls these.

    <T extends Event> void rose$addListener(Consumer<T> consumer, Class<T> eventType);

    <T extends Event> void rose$addListener(EventPriority priority, Consumer<T> consumer, Class<T> eventType);

    <T extends Event> void rose$addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer, Class<T> eventType);
}

package net.minecraftforge.eventbus.api;

import java.lang.reflect.Type;

/**
 * An event with a type argument ({@code AttachCapabilitiesEvent<Entity>}): a listener declared for one argument only
 * receives events whose generic type is exactly that class.
 */
public class GenericEvent<T> extends Event {
    private final Class<T> type;

    public GenericEvent(Class<T> type) {
        this.type = type;
    }

    public Type getGenericType() {
        return type;
    }
}

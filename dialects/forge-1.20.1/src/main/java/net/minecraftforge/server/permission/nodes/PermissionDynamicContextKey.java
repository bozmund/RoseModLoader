package net.minecraftforge.server.permission.nodes;

import java.util.function.Function;

/** A named, typed piece of context a permission check can carry. */
public record PermissionDynamicContextKey<T>(Class<T> typeToken, String name, Function<T, String> serializer) {
    public PermissionDynamicContext<T> createContext(T value) {
        return new PermissionDynamicContext<>(this, value);
    }
}

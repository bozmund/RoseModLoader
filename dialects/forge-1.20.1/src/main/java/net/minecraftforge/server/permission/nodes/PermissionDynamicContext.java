package net.minecraftforge.server.permission.nodes;

/** A context value passed with a permission check. */
public final class PermissionDynamicContext<T> {
    private final PermissionDynamicContextKey<T> dynamic;
    private final T value;

    public PermissionDynamicContext(PermissionDynamicContextKey<T> dynamic, T value) {
        this.dynamic = dynamic;
        this.value = value;
    }

    public PermissionDynamicContextKey<T> getDynamic() {
        return dynamic;
    }

    public T getValue() {
        return value;
    }

    public String getSerializedValue() {
        return dynamic.serializer().apply(value);
    }
}

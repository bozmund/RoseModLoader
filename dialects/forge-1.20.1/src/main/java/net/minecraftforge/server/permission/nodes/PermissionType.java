package net.minecraftforge.server.permission.nodes;

/** The value type of a permission node. */
public final class PermissionType<T> {
    private final Class<T> typeToken;
    private final String typeName;

    PermissionType(Class<T> typeToken, String typeName) {
        this.typeToken = typeToken;
        this.typeName = typeName;
    }

    public Class<T> typeToken() {
        return typeToken;
    }

    public String typeName() {
        return typeName;
    }
}

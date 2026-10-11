package net.minecraftforge.server.permission.nodes;

import net.minecraft.network.chat.Component;

/** The permission value types Forge 1.20.1 defines. */
public final class PermissionTypes {
    public static final PermissionType<Boolean> BOOLEAN = new PermissionType<>(Boolean.class, "boolean");
    public static final PermissionType<Integer> INTEGER = new PermissionType<>(Integer.class, "integer");
    public static final PermissionType<String> STRING = new PermissionType<>(String.class, "string");
    public static final PermissionType<Component> COMPONENT = new PermissionType<>(Component.class, "component");

    private PermissionTypes() {}
}

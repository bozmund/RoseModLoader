package net.minecraftforge.server.permission.nodes;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** A permission a mod checks: {@code modid.node}, its type and its default resolution. */
public final class PermissionNode<T> {
    private final String nodeName;
    private final PermissionType<T> type;
    private final PermissionResolver<T> defaultResolver;
    private final List<PermissionDynamicContextKey<?>> dynamics;
    private Component readableName;
    private Component description;

    public PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver,
                          PermissionDynamicContextKey<?>... dynamics) {
        this(modID + "." + nodeName, type, defaultResolver, dynamics);
    }

    public PermissionNode(net.minecraft.resources.Identifier nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver,
                          PermissionDynamicContextKey<?>... dynamics) {
        this(nodeName.getNamespace(), nodeName.getPath(), type, defaultResolver, dynamics);
    }

    private PermissionNode(String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) {
        this.nodeName = nodeName;
        this.type = type;
        this.defaultResolver = defaultResolver;
        this.dynamics = List.of(dynamics);
    }

    public PermissionNode<T> setInformation(Component readableName, Component description) {
        this.readableName = readableName;
        this.description = description;
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public PermissionType<T> getType() {
        return type;
    }

    public PermissionResolver<T> getDefaultResolver() {
        return defaultResolver;
    }

    public PermissionDynamicContextKey<?>[] getDynamics() {
        return dynamics.toArray(new PermissionDynamicContextKey<?>[0]);
    }

    public Component getReadableName() {
        return readableName;
    }

    public Component getDescription() {
        return description;
    }

    /** The value a node has when no permission handler overrides it. */
    @FunctionalInterface
    public interface PermissionResolver<T> {
        T resolve(ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context);
    }
}

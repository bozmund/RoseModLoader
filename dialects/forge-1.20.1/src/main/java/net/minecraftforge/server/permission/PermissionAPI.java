package net.minecraftforge.server.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionNode;

/**
 * Forge 1.20.1's permission checks with its default handler: every node resolves through its own default resolver
 * (Rose has no permission plugins).
 */
public final class PermissionAPI {
    private static final Set<PermissionNode<?>> NODES = Collections.synchronizedSet(new LinkedHashSet<>());

    public static <T> T getPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(player, player.getUUID(), context);
    }

    public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(null, player, context);
    }

    public static Collection<PermissionNode<?>> getRegisteredNodes() {
        return Collections.unmodifiableSet(NODES);
    }

    /** As the server is about to start (Forge's ServerLifecycleHooks): mods register their nodes. */
    public static void initializePermissionAPI() {
        PermissionGatherEvent.Nodes event = new PermissionGatherEvent.Nodes();
        MinecraftForge.EVENT_BUS.post(event);
        NODES.clear();
        NODES.addAll(event.getNodes());
    }

    private PermissionAPI() {}
}

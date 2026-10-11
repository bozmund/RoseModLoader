package net.minecraftforge.server.permission.events;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.server.permission.nodes.PermissionNode;

/** Forge's permission setup events (forge bus, as the server is about to start). */
public class PermissionGatherEvent extends Event {
    /** Mods add the permission nodes they check. */
    public static class Nodes extends PermissionGatherEvent {
        private final Set<PermissionNode<?>> nodes = new LinkedHashSet<>();

        public void addNodes(PermissionNode<?>... nodes) {
            Collections.addAll(this.nodes, nodes);
        }

        public void addNodes(Iterable<PermissionNode<?>> nodes) {
            nodes.forEach(this.nodes::add);
        }

        public Collection<PermissionNode<?>> getNodes() {
            return Collections.unmodifiableSet(nodes);
        }
    }
}

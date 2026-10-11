package net.minecraftforge.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.Event;

/** Mods add their own server data reload listeners (forge bus), run after vanilla's on every data load. */
public class AddReloadListenerEvent extends Event {
    private final List<PreparableReloadListener> listeners = new ArrayList<>();
    private final ReloadableServerResources serverResources;

    public AddReloadListenerEvent(ReloadableServerResources serverResources) {
        this.serverResources = serverResources;
    }

    public void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }

    public List<PreparableReloadListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public ReloadableServerResources getServerResources() {
        return serverResources;
    }

    /** Tags aren't loaded yet while listeners are gathered (as in Forge). */
    public ICondition.IContext getConditionContext() {
        return ICondition.IContext.EMPTY;
    }

    public HolderLookup.Provider getRegistryAccess() {
        return serverResources.fullRegistries().lookup();
    }
}

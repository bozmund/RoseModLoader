package net.minecraftforge.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.GenericEvent;

/** An object (entity, item stack) is gathering its capabilities: mods attach providers, keyed by id (forge bus). */
public class AttachCapabilitiesEvent<T> extends GenericEvent<T> {
    private final T object;
    private final Map<Identifier, ICapabilityProvider> caps = new LinkedHashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public AttachCapabilitiesEvent(Class<T> type, T object) {
        super(type);
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void addCapability(Identifier key, ICapabilityProvider cap) {
        if (caps.containsKey(key)) throw new IllegalStateException("Duplicate Capability Key: " + key + " " + cap);
        caps.put(key, cap);
    }

    public Map<Identifier, ICapabilityProvider> getCapabilities() {
        return Collections.unmodifiableMap(caps);
    }

    /** Run when the object's capabilities are invalidated. */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public List<Runnable> getListeners() {
        return Collections.unmodifiableList(listeners);
    }
}

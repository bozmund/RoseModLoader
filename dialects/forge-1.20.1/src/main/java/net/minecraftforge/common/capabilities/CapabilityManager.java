package net.minecraftforge.common.capabilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Hands out one {@link Capability} per type name. */
public final class CapabilityManager {
    private static final Map<String, Capability<?>> CAPABILITIES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Capability<T> get(CapabilityToken<T> type) {
        return (Capability<T>) CAPABILITIES.computeIfAbsent(type.getType(), Capability::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> Capability<T> get(Class<T> type) {
        return (Capability<T>) CAPABILITIES.computeIfAbsent(type.getName(), Capability::new);
    }

    private CapabilityManager() {}
}

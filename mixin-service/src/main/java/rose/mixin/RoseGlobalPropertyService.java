package rose.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

/** Mixin's global key/value store. Rose keeps it in memory; there is no launcher blackboard to share it with. */
public final class RoseGlobalPropertyService implements IGlobalPropertyService {
    private record Key(String name) implements IPropertyKey {}

    private final Map<IPropertyKey, Object> values = new ConcurrentHashMap<>();

    @Override public IPropertyKey resolveKey(String name) { return new Key(name); }

    @Override @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key) { return (T) values.get(key); }

    @Override public void setProperty(IPropertyKey key, Object value) { values.put(key, value); }

    @Override @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key, T defaultValue) { return (T) values.getOrDefault(key, defaultValue); }

    @Override public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object value = values.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}

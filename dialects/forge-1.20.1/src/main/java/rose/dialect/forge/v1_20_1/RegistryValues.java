package rose.dialect.forge.v1_20_1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import rose.dialect.forge.v1_20_1.mixin.MappedRegistryAccessor;

/**
 * Registry reads that also work while registries are open. 26.3 binds a holder's value only when its registry is
 * frozen, so {@code getValue} and iteration throw during registration; Forge mods expect them to work then.
 */
public final class RegistryValues {
    public static <T> T get(Registry<T> registry, Identifier id) {
        if (!(registry instanceof MappedRegistryAccessor<?>)) return registry.getValue(id);
        for (var entry : byValue(registry).entrySet()) {
            if (entry.getValue().key().identifier().equals(id)) return entry.getKey();
        }
        return null;
    }

    /** All values in registration order. */
    public static <T> List<T> all(Registry<T> registry) {
        if (!(registry instanceof MappedRegistryAccessor<?>)) return registry.stream().toList();
        List<Map.Entry<T, Holder.Reference<T>>> entries = new ArrayList<>(byValue(registry).entrySet());
        entries.sort(Comparator.comparingInt(e -> registry.getId(e.getKey())));
        return entries.stream().map(Map.Entry::getKey).toList();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<T, Holder.Reference<T>> byValue(Registry<T> registry) {
        return ((MappedRegistryAccessor<T>) registry).rose$byValue();
    }

    private RegistryValues() {}
}

package rose.era.v1_20_1;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * The registry entry an old mod's code is creating right now, set by the dialect around registration suppliers.
 * 26.3 wants keys in places 1.20.1 didn't (block/item properties, entity types).
 */
public final class RegistrationContext {
    private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

    public static <T> T supplying(ResourceKey<?> key, Supplier<T> supplier) {
        ResourceKey<?> previous = CURRENT.get();
        CURRENT.set(key);
        try {
            return supplier.get();
        } finally {
            CURRENT.set(previous);
        }
    }

    /** The key of the entry being created, if it belongs to {@code registry}. */
    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<T> current(ResourceKey<? extends Registry<T>> registry) {
        ResourceKey<?> key = CURRENT.get();
        return key != null && key.registry().equals(registry.identifier()) ? (ResourceKey<T>) key : null;
    }

    private RegistrationContext() {}
}

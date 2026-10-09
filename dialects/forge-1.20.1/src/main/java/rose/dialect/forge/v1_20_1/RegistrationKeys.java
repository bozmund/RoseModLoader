package rose.dialect.forge.v1_20_1;

import net.minecraft.resources.ResourceKey;

/**
 * Since 1.21.2 a block or item must know its registry key before it is constructed ({@code Properties.setId}).
 * Forge 1.20.1 mods construct them inside a registration supplier without one, so the dialect remembers which
 * entry is being created and the Properties mixins fill the id in from here.
 */
public final class RegistrationKeys {
    private static final ThreadLocal<ResourceKey<?>> CURRENT = new ThreadLocal<>();

    public static <T> T supplying(ResourceKey<?> key, java.util.function.Supplier<T> supplier) {
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
    public static <T> ResourceKey<T> current(ResourceKey<? extends net.minecraft.core.Registry<T>> registry) {
        ResourceKey<?> key = CURRENT.get();
        return key != null && key.registry().equals(registry.identifier()) ? (ResourceKey<T>) key : null;
    }

    private RegistrationKeys() {}
}

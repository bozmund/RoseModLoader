package rose.dialect.forge.v1_20_1;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import rose.era.v1_20_1.RegistrationContext;

/**
 * Since 1.21.2 a block or item must know its registry key before it is constructed ({@code Properties.setId}).
 * Forge 1.20.1 mods construct them inside a registration supplier without one, so the dialect remembers which
 * entry is being created (in the era bridge's {@link RegistrationContext}, which other 26.3-required keys use too)
 * and the Properties mixins fill the id in from there.
 */
public final class RegistrationKeys {
    public static <T> T supplying(ResourceKey<?> key, Supplier<T> supplier) {
        return RegistrationContext.supplying(key, supplier);
    }

    /** The key of the entry being created, if it belongs to {@code registry}. */
    public static <T> ResourceKey<T> current(ResourceKey<? extends Registry<T>> registry) {
        return RegistrationContext.current(registry);
    }

    private RegistrationKeys() {}
}

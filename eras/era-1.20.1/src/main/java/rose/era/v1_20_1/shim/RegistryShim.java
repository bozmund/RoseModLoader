package rose.era.v1_20_1.shim;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Redirect targets for 1.20.1 {@code Registry} lookups renamed in 1.21.2. */
public final class RegistryShim {
    /** 1.20.1 {@code get(ResourceLocation)}: 26.3 {@code getValue(Identifier)} (a defaulted registry still gives its default). */
    public static <T> T get(Registry<T> self, Identifier id) {
        return self.getValue(id);
    }

    /** 1.20.1 {@code getHolderOrThrow(ResourceKey)}: 26.3 {@code getOrThrow(ResourceKey)}. */
    public static <T> Holder.Reference<T> getHolderOrThrow(Registry<T> self, ResourceKey<T> key) {
        return self.getOrThrow(key);
    }

    /** 1.20.1 {@code getHolder(ResourceKey)}: 26.3 {@code get(ResourceKey)}. */
    public static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> self, ResourceKey<T> key) {
        return self.get(key);
    }

    private RegistryShim() {}
}

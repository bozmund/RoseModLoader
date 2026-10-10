package rose.era.v1_20_1.shim;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

/** Redirect targets for 1.20.1 {@code RegistryAccess}: 1.21.2 renamed registry/registryOrThrow to lookup/lookupOrThrow. */
public final class RegistryAccessShim {
    public static <E> Optional<Registry<E>> registry(RegistryAccess self, ResourceKey<? extends Registry<? extends E>> key) {
        return self.lookup(key);
    }

    public static <E> Registry<E> registryOrThrow(RegistryAccess self, ResourceKey<? extends Registry<? extends E>> key) {
        return self.lookupOrThrow(key);
    }

    private RegistryAccessShim() {}
}

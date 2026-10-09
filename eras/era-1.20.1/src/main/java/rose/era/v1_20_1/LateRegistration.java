package rose.era.v1_20_1;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import rose.era.v1_20_1.mixin.MappedRegistryAccess;

/**
 * Registers into a built-in registry after it was frozen. 1.20.1 mods registered some things (criterion triggers)
 * during common setup; 26.3 freezes built-in registries before that. Only for registries whose entries need nothing
 * from freezing but a bound value: no intrusive holders, no components, no tags on the new entry.
 */
public final class LateRegistration {
    public static synchronized <T> Holder.Reference<T> register(MappedRegistry<T> registry, ResourceKey<T> key, T value) {
        MappedRegistryAccess access = (MappedRegistryAccess) registry;
        boolean wasFrozen = access.rose$frozen();
        access.rose$setFrozen(false);
        try {
            Holder.Reference<T> holder = registry.register(key, value, RegistrationInfo.BUILT_IN);
            if (wasFrozen) {
                holder.bindValue(value);
                holder.bindTags(List.of());
            }
            return holder;
        } finally {
            access.rose$setFrozen(wasFrozen);
        }
    }

    private LateRegistration() {}
}

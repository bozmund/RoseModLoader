package rose.dialect.forge.v1_20_1;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * Finds the 26.3 registry behind a Forge registry key. Registries that aren't built in on 26.3 (Forge's own, or
 * ones that became data-driven like enchantments and biomes) get a detached stand-in so mods can still register
 * into them; the entries are reported as unsupported.
 */
public final class ForgeRegistryLookup {
    private static final Map<Identifier, ForgeRegistry<?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V> ForgeRegistry<V> get(Identifier registryName) {
        return (ForgeRegistry<V>) CACHE.computeIfAbsent(registryName, id -> {
            Registry<?> vanilla = (Registry<?>) RegistryValues.get((Registry) BuiltInRegistries.REGISTRY, id);
            if (vanilla != null) return new ForgeRegistry<>(vanilla, false);
            return new ForgeRegistry<>(new MappedRegistry(ResourceKey.createRegistryKey(id), Lifecycle.stable()), true);
        });
    }

    public static <V> ForgeRegistry<V> get(ResourceKey<? extends Registry<V>> key) {
        return get(key.identifier());
    }

    public static <V> ForgeRegistry<V> vanilla(String path) {
        return get(Identifier.withDefaultNamespace(path));
    }

    /** Every registry looked up so far. */
    public static java.util.Collection<ForgeRegistry<?>> known() {
        return java.util.List.copyOf(CACHE.values());
    }

    private ForgeRegistryLookup() {}
}

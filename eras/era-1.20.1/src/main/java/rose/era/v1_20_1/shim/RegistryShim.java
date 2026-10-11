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

    /** Registries whose entries own their holder (intrusive): they can't be added to once frozen. */
    private static final java.util.Set<Registry<?>> INTRUSIVE = java.util.Set.of(
            net.minecraft.core.registries.BuiltInRegistries.BLOCK, net.minecraft.core.registries.BuiltInRegistries.ITEM,
            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE, net.minecraft.core.registries.BuiltInRegistries.FLUID,
            net.minecraft.core.registries.BuiltInRegistries.GAME_EVENT);

    /**
     * 1.20.1 {@code Registry.register(registry, key, value)} from a mod: the value in its 26.3 form (RegistryAdapters,
     * e.g. a LootItemFunctionType becomes its codec), and into a frozen registry late (Forge kept vanilla registries
     * open until loading completed; mods registered loot function types in common setup). Returns the mod's object.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T register(Registry<? super T> registry, ResourceKey<T> key, T value) {
        Object registered = rose.era.v1_20_1.RegistryAdapters.adapt(value);
        if (registry instanceof net.minecraft.core.MappedRegistry mapped && !INTRUSIVE.contains(registry)
                && ((rose.era.v1_20_1.mixin.MappedRegistryAccess) mapped).rose$frozen()) {
            rose.era.v1_20_1.LateRegistration.register(mapped, (ResourceKey) key, registered);
        } else {
            Registry.register((Registry) registry, (ResourceKey) key, registered);
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T register(Registry<? super T> registry, Identifier id, T value) {
        ResourceKey<T> key = (ResourceKey<T>) ResourceKey.create((ResourceKey) registry.key(), id);
        return register(registry, key, value);
    }

    public static <T> T register(Registry<? super T> registry, String id, T value) {
        return register(registry, Identifier.parse(id), value);
    }

    private RegistryShim() {}
}

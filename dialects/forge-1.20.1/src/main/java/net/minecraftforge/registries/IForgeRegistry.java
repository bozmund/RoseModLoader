package net.minecraftforge.registries;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Forge's view of a registry. On Rose every one is backed by a vanilla {@link Registry}. */
public interface IForgeRegistry<V> extends Iterable<V> {
    ResourceKey<Registry<V>> getRegistryKey();

    Identifier getRegistryName();

    void register(String key, V value);

    void register(Identifier key, V value);

    boolean containsKey(Identifier key);

    boolean containsValue(V value);

    boolean isEmpty();

    V getValue(Identifier key);

    Identifier getKey(V value);

    Optional<ResourceKey<V>> getResourceKey(V value);

    Identifier getDefaultKey();

    Set<Identifier> getKeys();

    Collection<V> getValues();

    Set<Map.Entry<ResourceKey<V>, V>> getEntries();

    Codec<V> getCodec();

    Optional<Holder<V>> getHolder(ResourceKey<V> key);

    Optional<Holder<V>> getHolder(Identifier location);

    Optional<Holder<V>> getHolder(V value);
}

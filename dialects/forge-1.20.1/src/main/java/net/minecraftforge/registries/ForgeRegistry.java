package net.minecraftforge.registries;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import rose.dialect.forge.v1_20_1.RegistryValues;
import rose.dialect.forge.v1_20_1.Unsupported;

/** An {@link IForgeRegistry} over a vanilla registry. */
public class ForgeRegistry<V> implements IForgeRegistry<V> {
    private final Registry<V> vanilla;
    private final boolean detached;

    /**
     * @param detached the registry isn't one the game reads (a Forge-only or now data-driven registry); entries
     *                 are kept so mods can look them up, but they have no effect in game yet
     */
    public ForgeRegistry(Registry<V> vanilla, boolean detached) {
        this.vanilla = vanilla;
        this.detached = detached;
    }

    public Registry<V> vanilla() {
        return vanilla;
    }

    public boolean isDetached() {
        return detached;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResourceKey<Registry<V>> getRegistryKey() {
        return (ResourceKey<Registry<V>>) vanilla.key();
    }

    @Override
    public Identifier getRegistryName() {
        return vanilla.key().identifier();
    }

    @Override
    public void register(String key, V value) {
        register(Identifier.parse(key), value);
    }

    @Override
    public void register(Identifier key, V value) {
        rose.dialect.forge.v1_20_1.VanillaRegistration.register(vanilla, key, value);
    }

    @Override
    public boolean containsKey(Identifier key) {
        return vanilla.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return vanilla.getKey(value) != null;
    }

    @Override
    public boolean isEmpty() {
        return vanilla.keySet().isEmpty();
    }

    @Override
    public V getValue(Identifier key) {
        return RegistryValues.get(vanilla, key);
    }

    @Override
    public Identifier getKey(V value) {
        return vanilla.getKey(value);
    }

    @Override
    public Optional<ResourceKey<V>> getResourceKey(V value) {
        return vanilla.getResourceKey(value);
    }

    @Override
    public Identifier getDefaultKey() {
        return null;
    }

    @Override
    public Set<Identifier> getKeys() {
        return vanilla.keySet();
    }

    @Override
    public Collection<V> getValues() {
        return RegistryValues.all(vanilla);
    }

    @Override
    public Set<Map.Entry<ResourceKey<V>, V>> getEntries() {
        return vanilla.entrySet();
    }

    @Override
    public Codec<V> getCodec() {
        return vanilla.byNameCodec();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Holder<V>> getHolder(ResourceKey<V> key) {
        return (Optional<Holder<V>>) (Optional<?>) vanilla.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Holder<V>> getHolder(Identifier location) {
        return (Optional<Holder<V>>) (Optional<?>) vanilla.get(location);
    }

    @Override
    public Optional<Holder<V>> getHolder(V value) {
        return vanilla.getResourceKey(value).flatMap(this::getHolder);
    }

    /** Forge used aliases to rename entries; 26.3 rewrites old ids with DataFixers instead. */
    public void addAlias(Identifier from, Identifier to) {
        Unsupported.feature("alias:" + getRegistryName(), "registry aliases (" + from + " -> " + to + ") are ignored");
    }

    @Override
    public Iterator<V> iterator() {
        return RegistryValues.all(vanilla).iterator();
    }
}

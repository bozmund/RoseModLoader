package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.eventbus.api.IEventBus;
import rose.dialect.forge.v1_20_1.ForgeRegistryLookup;
import rose.era.v1_20_1.RegistryAdapters;
import rose.dialect.forge.v1_20_1.RegistrationKeys;
import rose.dialect.forge.v1_20_1.RegistryValues;
import rose.dialect.forge.v1_20_1.Unsupported;

/** Collects entries a mod wants registered and registers them when its registry's {@link RegisterEvent} fires. */
public class DeferredRegister<T> {
    private final ForgeRegistry<T> registry;
    private final String modid;
    private final Map<RegistryObject<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private boolean seenRegisterEvent;

    private DeferredRegister(ForgeRegistry<T> registry, String modid) {
        this.registry = registry;
        this.modid = modid;
    }

    public static <B> DeferredRegister<B> create(IForgeRegistry<B> registry, String modid) {
        return new DeferredRegister<>((ForgeRegistry<B>) registry, modid);
    }

    public static <B> DeferredRegister<B> create(ResourceKey<? extends Registry<B>> key, String modid) {
        return new DeferredRegister<>(ForgeRegistryLookup.get(key), modid);
    }

    public static <B> DeferredRegister<B> create(Identifier registryName, String modid) {
        return new DeferredRegister<>(ForgeRegistryLookup.get(registryName), modid);
    }

    public static <B> DeferredRegister<B> createOptional(ResourceKey<? extends Registry<B>> key, String modid) {
        return create(key, modid);
    }

    public static <B> DeferredRegister<B> createOptional(Identifier registryName, String modid) {
        return create(registryName, modid);
    }

    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        if (seenRegisterEvent) throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        RegistryObject<I> object = new RegistryObject<>(Identifier.fromNamespaceAndPath(modid, name), registry);
        if (entries.putIfAbsent((RegistryObject<T>) object, supplier) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }
        return object;
    }

    public void register(IEventBus bus) {
        bus.rose$addListener(this::addEntries, RegisterEvent.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addEntries(RegisterEvent event) {
        if (!event.getRegistryKey().equals(registry.getRegistryKey())) return;
        seenRegisterEvent = true;
        if (registry.isDetached() && !entries.isEmpty()) {
            Unsupported.registry(registry.getRegistryName().toString(), modid + " registers " + entries.size()
                    + " entries into " + registry.getRegistryName() + ", which isn't a built-in registry on 26.3; they are kept but have no effect yet");
        }
        for (var entry : entries.entrySet()) {
            RegistryObject<T> object = entry.getKey();
            ResourceKey key = object.getKey();
            T value;
            Object registered;
            var holdersBefore = RegistryValues.intrusiveHolderOwners();
            var initializers = ((rose.dialect.forge.v1_20_1.mixin.DataComponentInitializersAccessor) (Object)
                    net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS).rose$initializers();
            int initializersBefore = initializers.size();
            try {
                value = RegistrationKeys.supplying(key, entry.getValue());
                // The registry gets the 26.3 form of the value; the mod keeps the object it created.
                registered = RegistryAdapters.adapt(value);
            } catch (RuntimeException | LinkageError e) {
                // One entry Rose can't build yet (e.g. it uses a vanilla API that changed) shouldn't stop the mod.
                if (Unsupported.STRICT) throw e;
                RegistryValues.dropNewIntrusiveHolders(holdersBefore);
                while (initializers.size() > initializersBefore) initializers.removeLast();
                Unsupported.entry(object.getId().toString(), registry.getRegistryName().toString(), e);
                continue;
            }
            Registry.register((Registry) registry.vanilla(), key, registered);
            object.bind(value);
        }
    }

    public Collection<RegistryObject<T>> getEntries() {
        return Collections.unmodifiableCollection(new ArrayList<>(entries.keySet()));
    }

    public ResourceKey<Registry<T>> getRegistryKey() {
        return registry.getRegistryKey();
    }

    public Identifier getRegistryName() {
        return registry.getRegistryName();
    }

    public TagKey<T> createTagKey(String path) {
        return createTagKey(Identifier.fromNamespaceAndPath(modid, path));
    }

    public TagKey<T> createTagKey(Identifier location) {
        return TagKey.create(getRegistryKey(), location);
    }
}

package net.minecraftforge.registries;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.dialect.forge.v1_20_1.RegistrationKeys;

/** Fired on each mod bus once per registry, while registries are still open. */
public class RegisterEvent extends Event implements IModBusEvent {
    private final ResourceKey<? extends Registry<?>> registryKey;
    private final ForgeRegistry<?> registry;

    public RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, ForgeRegistry<?> registry) {
        this.registryKey = registryKey;
        this.registry = registry;
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return registryKey;
    }

    @SuppressWarnings("unchecked")
    public <T> IForgeRegistry<T> getForgeRegistry() {
        return (IForgeRegistry<T>) registry;
    }

    @SuppressWarnings("unchecked")
    public <T> Registry<T> getVanillaRegistry() {
        return (Registry<T>) registry.vanilla();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(ResourceKey<? extends Registry<T>> key, Identifier name, Supplier<T> valueSupplier) {
        if (!registryKey.equals(key)) return;
        ResourceKey entryKey = ResourceKey.create((ResourceKey) key, name);
        T value = RegistrationKeys.supplying(entryKey, valueSupplier);
        rose.dialect.forge.v1_20_1.VanillaRegistration.register(registry.vanilla(), entryKey, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void register(ResourceKey<? extends Registry<T>> key, Consumer<RegisterHelper<T>> consumer) {
        if (!registryKey.equals(key)) return;
        consumer.accept(new RegisterHelper<T>() {
            @Override
            public void register(Identifier name, T value) {
                rose.dialect.forge.v1_20_1.VanillaRegistration.register(registry.vanilla(), name, value);
            }

            @Override
            public void register(ResourceKey<T> entryKey, T value) {
                rose.dialect.forge.v1_20_1.VanillaRegistration.register(registry.vanilla(), entryKey, value);
            }
        });
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {
        default void register(String name, T value) {
            register(Identifier.parse(name), value);
        }

        void register(Identifier name, T value);

        default void register(ResourceKey<T> key, T value) {
            register(key.identifier(), value);
        }
    }
}

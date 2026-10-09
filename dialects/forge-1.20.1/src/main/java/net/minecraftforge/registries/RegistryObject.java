package net.minecraftforge.registries;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import rose.dialect.forge.v1_20_1.ForgeRegistryLookup;

/** A reference to a registry entry that exists once registration ran. */
public final class RegistryObject<T> implements Supplier<T> {
    private final Identifier name;
    private final ResourceKey<T> key;
    private final ForgeRegistry<? super T> registry;
    private T value;

    @SuppressWarnings({"unchecked", "rawtypes"})
    RegistryObject(Identifier name, ForgeRegistry<? super T> registry) {
        this.name = name;
        this.registry = registry;
        this.key = (ResourceKey<T>) ResourceKey.create((ResourceKey) registry.getRegistryKey(), name);
    }

    public static <T, U extends T> RegistryObject<U> create(Identifier name, IForgeRegistry<T> registry) {
        return new RegistryObject<>(name, (ForgeRegistry<T>) registry);
    }

    public static <T, U extends T> RegistryObject<U> create(Identifier name, ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new RegistryObject<>(name, ForgeRegistryLookup.get(registryKey));
    }

    public static <T, U extends T> RegistryObject<U> create(Identifier name, Identifier registryName, String modid) {
        return new RegistryObject<>(name, ForgeRegistryLookup.<T>get(registryName));
    }

    /** Called by DeferredRegister right after it registered the value. */
    void bind(T registered) {
        this.value = registered;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if (value == null) value = (T) registry.getValue(name);
        return Objects.requireNonNull(value, () -> "Registry Object not present: " + name);
    }

    public Identifier getId() {
        return name;
    }

    public ResourceKey<T> getKey() {
        return key;
    }

    public boolean isPresent() {
        return value != null || registry.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public Optional<Holder<T>> getHolder() {
        return (Optional<Holder<T>>) (Optional<?>) registry.getHolder(name);
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) consumer.accept(get());
    }

    public Optional<T> filter(Predicate<? super T> predicate) {
        return isPresent() && predicate.test(get()) ? Optional.of(get()) : Optional.empty();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return isPresent() ? Optional.ofNullable(mapper.apply(get())) : Optional.empty();
    }

    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return isPresent() ? mapper.apply(get()) : Optional.empty();
    }

    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    @Override
    public String toString() {
        return "RegistryObject[" + name + "]";
    }
}

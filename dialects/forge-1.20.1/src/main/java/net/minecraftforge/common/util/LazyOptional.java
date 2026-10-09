package net.minecraftforge.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** A value that is created on first use and can be invalidated (Forge capabilities hand these out). */
public class LazyOptional<T> {
    private static final LazyOptional<Void> EMPTY = new LazyOptional<>(null);

    private final NonNullSupplier<T> supplier;
    private T resolved;
    private boolean isValid = true;
    private final List<NonNullConsumer<LazyOptional<T>>> listeners = new ArrayList<>();

    private LazyOptional(NonNullSupplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyOptional<T> of(NonNullSupplier<T> supplier) {
        return supplier == null ? empty() : new LazyOptional<>(supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public <X> LazyOptional<X> cast() {
        return (LazyOptional<X>) this;
    }

    private T getValue() {
        if (!isValid || supplier == null) return null;
        if (resolved == null) resolved = Objects.requireNonNull(supplier.get(), "LazyOptional supplier returned null");
        return resolved;
    }

    public boolean isPresent() {
        return supplier != null && isValid;
    }

    public void ifPresent(NonNullConsumer<? super T> consumer) {
        T value = getValue();
        if (value != null) consumer.accept(value);
    }

    public <U> LazyOptional<U> lazyMap(NonNullFunction<? super T, ? extends U> mapper) {
        return isPresent() ? of(() -> mapper.apply(getValue())) : empty();
    }

    public <U> Optional<U> map(NonNullFunction<? super T, ? extends U> mapper) {
        return isPresent() ? Optional.ofNullable(mapper.apply(getValue())) : Optional.empty();
    }

    public Optional<T> filter(NonNullPredicate<? super T> predicate) {
        T value = getValue();
        return value != null && predicate.test(value) ? Optional.of(value) : Optional.empty();
    }

    public Optional<T> resolve() {
        return isPresent() ? Optional.of(getValue()) : Optional.empty();
    }

    public T orElse(T other) {
        T value = getValue();
        return value != null ? value : other;
    }

    public T orElseGet(NonNullSupplier<? extends T> other) {
        T value = getValue();
        return value != null ? value : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        T value = getValue();
        if (value != null) return value;
        throw exceptionSupplier.get();
    }

    public T orElseThrow() {
        T value = getValue();
        if (value == null) throw new java.util.NoSuchElementException("No value present");
        return value;
    }

    public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
        if (isPresent()) listeners.add(listener);
        else listener.accept(this);
    }

    public void invalidate() {
        if (!isValid) return;
        isValid = false;
        listeners.forEach(l -> l.accept(this));
    }
}

package net.minecraftforge.common.util;

@FunctionalInterface
public interface NonNullPredicate<T> {
    boolean test(T t);
}

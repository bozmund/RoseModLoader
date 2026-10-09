package net.minecraft.advancements.critereon;

/**
 * 1.20.1 {@code ContextAwarePredicate}: the "player" condition of a criterion. 1.20.5 replaced it with an optional
 * loot condition holder; Rose keeps the class so old trigger code links. Only {@link #ANY} carries meaning.
 */
public class ContextAwarePredicate {
    public static final ContextAwarePredicate ANY = new ContextAwarePredicate();
}

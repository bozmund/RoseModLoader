package net.minecraftforge.registries.tags;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/** A registry tag's current members (empty until tags load). */
public interface ITag<V> extends Iterable<V> {
    TagKey<V> getKey();

    Stream<V> stream();

    boolean isEmpty();

    int size();

    boolean contains(V value);

    Optional<V> getRandomElement(RandomSource random);

    boolean isBound();
}

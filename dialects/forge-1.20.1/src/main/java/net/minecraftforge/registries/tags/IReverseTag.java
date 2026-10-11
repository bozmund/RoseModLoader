package net.minecraftforge.registries.tags;

import java.util.stream.Stream;
import net.minecraft.tags.TagKey;

/** The tags one registry value is in. */
public interface IReverseTag<V> {
    Stream<TagKey<V>> getTagKeys();

    boolean containsTag(TagKey<V> key);
}

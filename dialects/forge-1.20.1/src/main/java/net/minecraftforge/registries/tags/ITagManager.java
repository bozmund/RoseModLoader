package net.minecraftforge.registries.tags;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/** A Forge registry's tags (Forge's view of the vanilla registry's tags). */
public interface ITagManager<V> extends Iterable<ITag<V>> {
    ITag<V> getTag(TagKey<V> name);

    Optional<IReverseTag<V>> getReverseTag(V value);

    boolean isKnownTagName(TagKey<V> name);

    Stream<ITag<V>> stream();

    Stream<TagKey<V>> getTagNames();

    TagKey<V> createTagKey(Identifier location);
}

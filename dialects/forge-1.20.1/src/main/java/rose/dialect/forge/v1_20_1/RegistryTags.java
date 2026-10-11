package rose.dialect.forge.v1_20_1;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

/** Forge's {@code IForgeRegistry.tags()} over a vanilla registry: always reads the registry's current tags. */
public final class RegistryTags<V> implements ITagManager<V> {
    private final Registry<V> registry;

    public RegistryTags(Registry<V> registry) {
        this.registry = registry;
    }

    @Override
    public ITag<V> getTag(TagKey<V> name) {
        return new Tag<>(registry, name);
    }

    @Override
    public Optional<IReverseTag<V>> getReverseTag(V value) {
        return registry.getResourceKey(value).flatMap(registry::get).map(holder -> new IReverseTag<V>() {
            @Override
            public Stream<TagKey<V>> getTagKeys() {
                return holder.tags();
            }

            @Override
            public boolean containsTag(TagKey<V> key) {
                return holder.is(key);
            }
        });
    }

    @Override
    public boolean isKnownTagName(TagKey<V> name) {
        return registry.get(name).isPresent();
    }

    @Override
    public Stream<ITag<V>> stream() {
        return getTagNames().map(this::getTag);
    }

    @Override
    public Stream<TagKey<V>> getTagNames() {
        return registry.getTags().map(HolderSet.Named::key);
    }

    @Override
    public TagKey<V> createTagKey(Identifier location) {
        return TagKey.create(registry.key(), location);
    }

    @Override
    public Iterator<ITag<V>> iterator() {
        return stream().iterator();
    }

    private record Tag<V>(Registry<V> registry, TagKey<V> key) implements ITag<V> {
        private Optional<HolderSet.Named<V>> set() {
            return registry.get(key);
        }

        @Override
        public TagKey<V> getKey() {
            return key;
        }

        @Override
        public Stream<V> stream() {
            return set().stream().flatMap(HolderSet::stream).map(Holder::value);
        }

        @Override
        public boolean isEmpty() {
            return set().map(s -> s.size() == 0).orElse(true);
        }

        @Override
        public int size() {
            return set().map(HolderSet::size).orElse(0);
        }

        @Override
        public boolean contains(V value) {
            return set().map(s -> s.stream().anyMatch(h -> h.value() == value)).orElse(false);
        }

        @Override
        public Optional<V> getRandomElement(RandomSource random) {
            return set().flatMap(s -> s.getRandomElement(random)).map(Holder::value);
        }

        @Override
        public boolean isBound() {
            return set().isPresent();
        }

        @Override
        public Iterator<V> iterator() {
            return stream().iterator();
        }
    }
}

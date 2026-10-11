package rose.era.v1_20_1.shim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import rose.era.v1_20_1.EraContext;

/**
 * 1.20.1 {@code FriendlyByteBuf} item and text methods. 1.20.5 moved them to registry-aware stream codecs
 * ({@code ItemStack.OPTIONAL_STREAM_CODEC}, {@code ComponentSerialization.STREAM_CODEC}); a plain buffer is wrapped with
 * the game's registries.
 */
public final class FriendlyByteBufShim {
    private FriendlyByteBufShim() {}

    public static FriendlyByteBuf writeItem(FriendlyByteBuf buf, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryAware(buf), stack);
        return buf;
    }

    public static ItemStack readItem(FriendlyByteBuf buf) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(registryAware(buf));
    }

    public static FriendlyByteBuf writeComponent(FriendlyByteBuf buf, Component component) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryAware(buf), component);
        return buf;
    }

    public static Component readComponent(FriendlyByteBuf buf) {
        return ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryAware(buf));
    }

    // 1.20.1's collection methods: a var-int count, then each element (the same bytes 1.20.1 wrote).

    public static <T> void writeCollection(FriendlyByteBuf buf, java.util.Collection<T> values, net.minecraft.network.FriendlyByteBuf$Writer<T> writer) {
        buf.writeVarInt(values.size());
        for (T value : values) writer.accept(buf, value);
    }

    public static <T, C extends java.util.Collection<T>> C readCollection(FriendlyByteBuf buf, java.util.function.IntFunction<C> factory,
                                                                         net.minecraft.network.FriendlyByteBuf$Reader<T> reader) {
        int size = buf.readVarInt();
        C out = factory.apply(size);
        for (int i = 0; i < size; i++) out.add(reader.apply(buf));
        return out;
    }

    public static <T> java.util.List<T> readList(FriendlyByteBuf buf, net.minecraft.network.FriendlyByteBuf$Reader<T> reader) {
        return readCollection(buf, java.util.ArrayList::new, reader);
    }

    public static <K, V> void writeMap(FriendlyByteBuf buf, java.util.Map<K, V> map, net.minecraft.network.FriendlyByteBuf$Writer<K> keys,
                                       net.minecraft.network.FriendlyByteBuf$Writer<V> values) {
        buf.writeVarInt(map.size());
        map.forEach((k, v) -> {
            keys.accept(buf, k);
            values.accept(buf, v);
        });
    }

    public static <K, V, M extends java.util.Map<K, V>> M readMap(FriendlyByteBuf buf, java.util.function.IntFunction<M> factory,
                                                                 net.minecraft.network.FriendlyByteBuf$Reader<K> keys, net.minecraft.network.FriendlyByteBuf$Reader<V> values) {
        int size = buf.readVarInt();
        M out = factory.apply(size);
        for (int i = 0; i < size; i++) out.put(keys.apply(buf), values.apply(buf));
        return out;
    }

    public static <K, V> java.util.Map<K, V> readMap(FriendlyByteBuf buf, net.minecraft.network.FriendlyByteBuf$Reader<K> keys, net.minecraft.network.FriendlyByteBuf$Reader<V> values) {
        return readMap(buf, com.google.common.collect.Maps::newHashMapWithExpectedSize, keys, values);
    }

    public static <T> void writeOptional(FriendlyByteBuf buf, java.util.Optional<T> value, net.minecraft.network.FriendlyByteBuf$Writer<T> writer) {
        buf.writeOptional(value, writer::accept);
    }

    public static <T> java.util.Optional<T> readOptional(FriendlyByteBuf buf, net.minecraft.network.FriendlyByteBuf$Reader<T> reader) {
        return buf.readOptional(reader::apply);
    }

    private static RegistryFriendlyByteBuf registryAware(FriendlyByteBuf buf) {
        return buf instanceof RegistryFriendlyByteBuf r ? r : new RegistryFriendlyByteBuf(buf, EraContext.registryAccess());
    }
}

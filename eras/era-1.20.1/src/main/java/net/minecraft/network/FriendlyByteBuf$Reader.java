package net.minecraft.network;

import java.util.function.Function;

/** Era bridge (1.20.1): {@code FriendlyByteBuf.Reader}, reads one value (see FriendlyByteBuf$Writer). */
@FunctionalInterface
public interface FriendlyByteBuf$Reader<T> extends Function<FriendlyByteBuf, T> {}

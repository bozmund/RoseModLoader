package net.minecraft.network;

import java.util.function.BiConsumer;

/**
 * Era bridge (1.20.1): {@code FriendlyByteBuf.Writer}, writes one value. 26.x uses StreamEncoder; the buffer methods
 * that took writers are shims (FriendlyByteBufShim). A top-level type named like the old nested one.
 */
@FunctionalInterface
public interface FriendlyByteBuf$Writer<T> extends BiConsumer<FriendlyByteBuf, T> {}

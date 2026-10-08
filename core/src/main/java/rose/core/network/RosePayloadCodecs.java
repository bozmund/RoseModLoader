package rose.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import rose.api.network.RoseNetworking;

/**
 * Vanilla decodes custom payloads through a fixed id -> codec map and falls back to a "discard" codec for unknown
 * ids. Rose wraps that fallback so ids registered through {@link RoseNetworking} resolve to the mod's codec.
 * Looking up at decode time (not class-init time) means mods can register after the packet classes have loaded.
 */
public final class RosePayloadCodecs {
    public static <B extends FriendlyByteBuf> CustomPacketPayload.FallbackProvider<B> wrap(
            CustomPacketPayload.FallbackProvider<B> vanilla) {
        return id -> {
            RoseNetworking.Registration<?> registration = RoseNetworking.registration(id);
            return registration != null ? playOnly(registration) : vanilla.create(id);
        };
    }

    /** Rose payloads are gameplay-phase payloads; their codecs may need the registry-aware buffer. */
    private static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> playOnly(
            RoseNetworking.Registration<T> registration) {
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = registration.codec();
        return new StreamCodec<>() {
            @Override
            public T decode(B buf) {
                return codec.decode(require(buf, registration));
            }

            @Override
            public void encode(B buf, T value) {
                codec.encode(require(buf, registration), value);
            }
        };
    }

    private static RegistryFriendlyByteBuf require(ByteBuf buf, RoseNetworking.Registration<?> registration) {
        if (buf instanceof RegistryFriendlyByteBuf registryBuf) return registryBuf;
        throw new IllegalStateException("Payload " + registration.type().id()
                + " can only be sent during gameplay, not in the login/configuration phase");
    }

    private RosePayloadCodecs() {}
}

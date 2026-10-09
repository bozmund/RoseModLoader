package net.minecraftforge.network.simple;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class IndexedMessageCodec {
    public static class MessageHandler<MSG> {
        final int index;
        final Class<MSG> messageType;
        final BiConsumer<MSG, FriendlyByteBuf> encoder;
        final Function<FriendlyByteBuf, MSG> decoder;
        final BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer;

        MessageHandler(int index, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder,
                       Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {
            this.index = index;
            this.messageType = messageType;
            this.encoder = encoder;
            this.decoder = decoder;
            this.consumer = consumer;
        }
    }
}

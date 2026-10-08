package rose.testmods.sample;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: the counter at {@code pos} now shows {@code count}. */
public record CountPayload(BlockPos pos, int count) implements CustomPacketPayload {
    public static final Type<CountPayload> TYPE = new Type<>(SampleMod.id("count"));
    public static final StreamCodec<ByteBuf, CountPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CountPayload::pos,
            ByteBufCodecs.VAR_INT, CountPayload::count,
            CountPayload::new);

    @Override
    public Type<CountPayload> type() {
        return TYPE;
    }
}

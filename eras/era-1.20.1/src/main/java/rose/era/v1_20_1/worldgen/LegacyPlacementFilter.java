package rose.era.v1_20_1.worldgen;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

/** 1.20.1 {@code PlacementFilter}: keeps the origin if {@code shouldPlace}. */
public abstract class LegacyPlacementFilter extends LegacyPlacementModifier {
    @Override
    public final Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        return shouldPlace(context, random, origin) ? Stream.of(origin) : Stream.empty();
    }

    protected abstract boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin);
}

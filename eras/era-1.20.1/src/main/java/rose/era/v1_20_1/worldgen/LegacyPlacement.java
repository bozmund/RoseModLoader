package rose.era.v1_20_1.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

/** A 1.20.1 placement modifier as a 26.3 {@code PlacementModifier}. */
public record LegacyPlacement(LegacyPlacementModifier legacy, MapCodec<? extends PlacementModifier> codec) implements PlacementModifier {
    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        legacy.getPositions(context, random, origin).forEach(output);
    }
}

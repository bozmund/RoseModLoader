package rose.era.v1_20_1.worldgen;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * 1.20.1 {@code PlacementModifier} (an abstract class; 26.3's is an interface with {@code modify(.., output)}). Mod
 * modifiers are rebased onto this and run inside {@link LegacyPlacement}, so their protected methods stay callable.
 */
public abstract class LegacyPlacementModifier {
    public abstract Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin);

    public abstract PlacementModifierType<?> type();
}

package rose.era.v1_20_1.shim;

import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/** Redirect targets for 1.20.1 {@code BlockBehaviour.Properties}. */
public final class BlockPropertiesShim {
    /**
     * 1.20.1 {@code Properties.copy(block)} is 26.3's {@code ofLegacyCopy} (not ofFullCopy, which also copies the loot
     * table and name). 26.x made some copied functions read the source block's own properties (wheat's map color
     * depends on its AGE); on the copying block those properties may not exist, so they fall back to the source
     * block's default state.
     */
    public static BlockBehaviour.Properties copy(BlockBehaviour block) {
        BlockBehaviour.Properties copy = BlockBehaviour.Properties.ofLegacyCopy(block);
        BlockState fallback = ((net.minecraft.world.level.block.Block) block).defaultBlockState();
        Function<BlockState, MapColor> mapColor = copy.mapColor;
        ToIntFunction<BlockState> light = copy.lightEmission;
        copy.mapColor(state -> {
            try {
                return mapColor.apply(state);
            } catch (IllegalArgumentException missingProperty) {
                return mapColor.apply(fallback);
            }
        });
        copy.lightLevel(state -> {
            try {
                return light.applyAsInt(state);
            } catch (IllegalArgumentException missingProperty) {
                return light.applyAsInt(fallback);
            }
        });
        return copy;
    }

    private BlockPropertiesShim() {}
}

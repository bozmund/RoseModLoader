package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge 1.20.1 made BushBlock, the base of plants that need soil, an IPlantable; 26.3 calls that class
 * VegetationBlock. Forge mods pass their plants as IPlantable to soil checks (canSustainPlant). getPlant as in Forge's
 * BushBlock patch; the plant type stays IPlantable's default, PLAINS.
 */
@Mixin(VegetationBlock.class)
public abstract class VegetationBlockMixin implements IPlantable {
    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block self = (Block) (Object) this;
        return state.getBlock() == self ? state : self.defaultBlockState();
    }
}

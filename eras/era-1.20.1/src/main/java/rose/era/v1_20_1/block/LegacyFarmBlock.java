package rose.era.v1_20_1.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Era bridge (1.20.1): FarmBlock's constructor took only properties. 26.3 FarmlandBlock also takes the block it turns
 * back into when trampled or dried out; 1.20.1 farmland always became dirt.
 */
public class LegacyFarmBlock extends FarmlandBlock {
    public LegacyFarmBlock(BlockBehaviour.Properties properties) {
        super(Blocks.DIRT, properties);
    }
}

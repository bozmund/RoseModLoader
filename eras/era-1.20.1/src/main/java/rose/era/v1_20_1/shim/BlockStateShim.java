package rose.era.v1_20_1.shim;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Redirect targets for 1.20.1 {@code BlockState} methods that changed in 26.3. */
public final class BlockStateShim {
    /** 1.20.1 {@code BlockState.is(Block)}: {@code getBlock() == block}. */
    public static boolean is(BlockState self, Block block) {
        return self.getBlock() == block;
    }

    private BlockStateShim() {}

    /**
     * 1.20.1 {@code RenderShape.ENTITYBLOCK_ANIMATED} (no chunk model; a block entity renderer draws it). 26.x dropped
     * it: vanilla chests are MODEL blocks with a particle-only model, and their renderer still draws them.
     */
    public static net.minecraft.world.level.block.RenderShape ENTITYBLOCK_ANIMATED() {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
}

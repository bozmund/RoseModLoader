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

    private static final java.lang.invoke.MethodType GET_EXP_DROP = java.lang.invoke.MethodType.methodType(int.class, BlockState.class,
            net.minecraft.world.level.LevelReader.class, net.minecraft.util.RandomSource.class, net.minecraft.core.BlockPos.class, int.class, int.class);

    /**
     * Forge 1.20.1 {@code IForgeBlockState.getExpDrop(level, random, pos, fortune, silkTouch)}: the experience the block
     * drops when broken (a mod block's own getExpDrop override, else Forge's amounts for vanilla blocks). 26.3 blocks
     * pop the same amounts in spawnAfterBreak.
     */
    public static int getExpDrop(BlockState self, net.minecraft.world.level.LevelReader level, net.minecraft.util.RandomSource random,
                                 net.minecraft.core.BlockPos pos, int fortune, int silkTouch) {
        Block block = self.getBlock();
        var own = rose.era.v1_20_1.bridge.Legacy.find(block, "getExpDrop", GET_EXP_DROP);
        if (own.isPresent()) return (Integer) rose.era.v1_20_1.bridge.Legacy.invoke(own.get(), block, self, level, random, pos, fortune, silkTouch);
        if (block instanceof net.minecraft.world.level.block.SpawnerBlock) return 15 + random.nextInt(15) + random.nextInt(15);
        if (silkTouch > 0) return 0;
        if (block instanceof net.minecraft.world.level.block.DropExperienceBlock ore) return ore.xpRange.sample(random);
        if (block instanceof net.minecraft.world.level.block.RedStoneOreBlock) return 1 + random.nextInt(5);
        if (block instanceof net.minecraft.world.level.block.SculkCatalystBlock
                || block instanceof net.minecraft.world.level.block.SculkSensorBlock
                || block instanceof net.minecraft.world.level.block.SculkShriekerBlock) return 5;
        return 0;
    }

    /**
     * 1.20.1 {@code RenderShape.ENTITYBLOCK_ANIMATED} (no chunk model; a block entity renderer draws it). 26.x dropped
     * it: vanilla chests are MODEL blocks with a particle-only model, and their renderer still draws them.
     */
    public static net.minecraft.world.level.block.RenderShape ENTITYBLOCK_ANIMATED() {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
}

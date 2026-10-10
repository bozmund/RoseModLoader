package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Redirect targets for 1.20.1 {@code BlockSource} (an interface, implemented by BlockSourceImpl); 1.20.2 made it the
 * record {@code BlockSource(level, pos, state, blockEntity)}. Its {@code x()/y()/z()} aren't redirected: their SRG
 * names are Position's, which Vec3 still implements.
 */
public final class BlockSourceShim {
    public static BlockState getBlockState(BlockSource self) {
        return self.state();
    }

    public static ServerLevel getLevel(BlockSource self) {
        return self.level();
    }

    public static BlockPos getPos(BlockSource self) {
        return self.pos();
    }

    public static BlockEntity getEntity(BlockSource self) {
        return self.blockEntity();
    }

    private BlockSourceShim() {}
}

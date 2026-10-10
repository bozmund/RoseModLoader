package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

/** Redirect targets for 1.20.1 container opening (ContainerOpenersCounter) and Direction.getNormal. */
public final class ContainerShim {
    /**
     * 1.20.1 {@code incrementOpeners(player, level, pos, state)}; 26.x also takes how far the opener may reach, which
     * vanilla containers pass as the user's container interaction range (26.3 BarrelBlockEntity.startOpen).
     */
    public static void incrementOpeners(ContainerOpenersCounter self, Player player, Level level, BlockPos pos, BlockState state) {
        self.incrementOpeners(player, level, pos, state, player.getContainerInteractionRange());
    }

    /** 1.20.1 {@code Direction.getNormal()}; 26.3 {@code getUnitVec3i()}. */
    public static Vec3i getNormal(Direction self) {
        return self.getUnitVec3i();
    }

    private ContainerShim() {}
}

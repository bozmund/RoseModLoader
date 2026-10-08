package rose.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Vanilla gives every block and fluid state its network id (and builds block state caches) once, in the static
 * initializers of {@code Blocks} and {@code Fluids}. That runs before mods register anything, so mod states would
 * have no id: the first block-update packet for a mod block fails to encode and the player is disconnected.
 * Rose assigns the missing ids after mod initialization, in registry order, so client and server agree.
 */
public final class StateIds {
    public static void assignMissing() {
        for (Block block : BuiltInRegistries.BLOCK) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                if (Block.BLOCK_STATE_REGISTRY.getId(state) == -1) {
                    Block.BLOCK_STATE_REGISTRY.add(state);
                    state.initCache();
                }
            }
        }
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            for (FluidState state : fluid.getStateDefinition().getPossibleStates()) {
                if (Fluid.FLUID_STATE_REGISTRY.getId(state) == -1) Fluid.FLUID_STATE_REGISTRY.add(state);
            }
        }
    }

    private StateIds() {}
}

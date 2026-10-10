package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.shim.ForgeBlockShim;

/**
 * Forge 1.20.1 made BushBlock, the base of plants that need soil, an IPlantable; 26.3 calls that class
 * VegetationBlock. Forge mods pass their plants as IPlantable to soil checks (canSustainPlant). getPlant as in Forge's
 * BushBlock patch; the plant type stays IPlantable's default, PLAINS, unless a subclass says otherwise (crops).
 *
 * <p>Forge's BushBlock.canSurvive also asked the soil ({@code soil.canSustainPlant(level, below, UP, this)}). 26.3
 * plants check their {@code supports_*} tags instead, which mod soils from 1.20.1 aren't in; a mod soil that decides
 * itself (overrides canSustainPlant, e.g. FD's rich soil farmland) is asked as Forge did.
 */
@Mixin(VegetationBlock.class)
public abstract class VegetationBlockMixin implements IPlantable {
    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block self = (Block) (Object) this;
        return state.getBlock() == self ? state : self.defaultBlockState();
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void rose$modSoil(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() != (Object) this) return;
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        if (ForgeBlockShim.decidesPlants(soil.getBlock())) {
            cir.setReturnValue(ForgeBlockShim.canSustainPlant(soil, level, below, Direction.UP, this));
        }
    }
}

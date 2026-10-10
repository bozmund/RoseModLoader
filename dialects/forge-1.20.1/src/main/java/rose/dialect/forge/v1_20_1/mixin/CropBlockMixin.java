package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Mixin;

/** Forge 1.20.1's CropBlock patch: crops are of the CROP plant type (mod soils sustain plants by type). */
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.CROP;
    }
}

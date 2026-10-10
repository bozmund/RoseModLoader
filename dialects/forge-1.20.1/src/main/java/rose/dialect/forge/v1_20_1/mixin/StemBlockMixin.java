package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StemBlock;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Mixin;

/** Forge 1.20.1's StemBlock patch: melon and pumpkin stems are crops. */
@Mixin(StemBlock.class)
public abstract class StemBlockMixin {
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.CROP;
    }
}

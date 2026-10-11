package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.BreakExperience;

/** Holds back a block's own experience while a BreakEvent listener's amount replaces it (see BreakExperience). */
@Mixin(Block.class)
public abstract class BlockExperienceMixin {
    @Inject(method = "popExperience", at = @At("HEAD"), cancellable = true)
    private void rose$replaced(ServerLevel level, BlockPos pos, int amount, CallbackInfo ci) {
        if (BreakExperience.overridden()) ci.cancel();
    }
}

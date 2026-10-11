package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.BreakExperience;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/**
 * Forge 1.20.1 BlockEvent.BreakEvent before a player breaks a block (ForgeHooks.onBlockBreakEvent); canceled keeps it.
 * A listener's experience amount replaces the block's own.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class BlockBreakEventMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void rose$breakEvent(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeDialect.active()) return;
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        int experience = event.getExpToDrop();
        if (MinecraftForge.EVENT_BUS.post(event)) {
            level.sendBlockUpdated(pos, state, state, 3);
            cir.setReturnValue(false);
        } else if (event.getExpToDrop() != experience) {
            BreakExperience.override(event.getExpToDrop());
        }
    }

    /** Forge dropped the event's experience once the block was broken (not in creative). */
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void rose$breakExperience(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        int experience = BreakExperience.take();
        if (experience > 0 && cir.getReturnValueZ() && !player.isCreative()
                && level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.BLOCK_DROPS)) {
            net.minecraft.world.entity.ExperienceOrb.award(level, net.minecraft.world.phys.Vec3.atCenterOf(pos), experience);
        }
    }
}

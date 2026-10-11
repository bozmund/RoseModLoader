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
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 BlockEvent.BreakEvent before a player breaks a block (ForgeHooks.onBlockBreakEvent); canceled keeps it. */
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
        if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) {
            level.sendBlockUpdated(pos, state, state, 3);
            cir.setReturnValue(false);
        }
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.ForgeItemHooks;

/**
 * Forge 1.20.1 RightClickBlock on the server, before vanilla uses the item on the block (ForgeHooks.onRightClickBlock),
 * then the item's onItemUseFirst unless the event denied the item.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class InteractionEventsMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void rose$rightClickBlock(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
                                      CallbackInfoReturnable<InteractionResult> cir) {
        if (!ForgeDialect.active()) return;
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            cir.setReturnValue(event.getCancellationResult());
            return;
        }
        if (player.isSpectator() || event.getUseItem() == Event.Result.DENY) return;
        InteractionResult first = ForgeItemHooks.onItemUseFirst(stack, new UseOnContext(player, hand, hit));
        if (first != InteractionResult.PASS) cir.setReturnValue(first);
    }
}

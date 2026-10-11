package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
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
 * Forge 1.20.1 RightClickBlock on the client, before the client predicts using an item on a block, then the item's
 * onItemUseFirst unless the event denied the item.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class ClientInteractionEventsMixin {
    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void rose$rightClickBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ForgeDialect.active()) return;
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            cir.setReturnValue(event.getCancellationResult());
            return;
        }
        if (player.isSpectator() || event.getUseItem() == Event.Result.DENY) return;
        InteractionResult first = ForgeItemHooks.onItemUseFirst(player.getItemInHand(hand), new UseOnContext(player, hand, hit));
        if (first != InteractionResult.PASS) cir.setReturnValue(first);
    }
}

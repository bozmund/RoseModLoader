package rose.era.v1_20_1.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.item.LegacyItemComponents;

/**
 * 1.20.1 items chose their repair materials by overriding {@code Item.isValidRepairItem(stack, repair)}; 26.3 only
 * reads the {@code repairable} component. A predicate can't become a component (it may look at anything), so the
 * anvil asks the old override instead.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackRepairMixin {
    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    private void rose$legacyRepairItem(ItemStack repairItem, CallbackInfoReturnable<Boolean> cir) {
        Boolean legacy = LegacyItemComponents.isValidRepairItem((ItemStack) (Object) this, repairItem);
        if (legacy != null) cir.setReturnValue(legacy);
    }
}

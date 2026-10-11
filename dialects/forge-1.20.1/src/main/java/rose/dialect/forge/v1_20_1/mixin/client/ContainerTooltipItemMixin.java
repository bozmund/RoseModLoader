package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.client.TooltipItem;

/** The hovered slot's stack is the item of the tooltip a container screen draws (TooltipComponentsMixin). */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerTooltipItemMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void rose$hovered(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipItem.set(hoveredSlot != null && hoveredSlot.hasItem() ? hoveredSlot.getItem() : null);
    }

    @Inject(method = "extractTooltip", at = @At("RETURN"))
    private void rose$done(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipItem.set(null);
    }
}

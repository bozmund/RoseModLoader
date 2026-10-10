package rose.dialect.forge.v1_20_1.mixin.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 ItemTooltipEvent once an item's tooltip lines are built; listeners may add or change lines. */
@Mixin(ItemStack.class)
public abstract class TooltipEventsMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void rose$tooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        if (!ForgeDialect.active()) return;
        List<Component> lines = new ArrayList<>(cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(new ItemTooltipEvent((ItemStack) (Object) this, player, lines, flag));
        cir.setReturnValue(lines);
    }
}

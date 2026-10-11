package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeFurnaceFuel;

/** The furnace screen's fuel slot accepts what Forge's FurnaceFuelBurnTimeEvent calls fuel. */
@Mixin(AbstractFurnaceMenu.class)
public abstract class FurnaceMenuFuelMixin {
    @ModifyReturnValue(method = "isFuel", at = @At("RETURN"))
    private boolean rose$isFuel(boolean vanilla, ItemStack stack) {
        return ForgeFurnaceFuel.isFuel(stack, vanilla, RecipeType.SMELTING);
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeFurnaceFuel;

/** Furnaces ask Forge's FurnaceFuelBurnTimeEvent for a fuel's burn time and whether hoppers may insert it. */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceFuelMixin {
    @ModifyReturnValue(method = "getBurnDuration", at = @At("RETURN"))
    private int rose$burnTime(int vanilla, ServerLevel level, ItemStack fuel) {
        return ForgeFurnaceFuel.burnTime(fuel, vanilla, rose$recipeType());
    }

    @WrapOperation(method = "canPlaceItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"))
    private boolean rose$isFuel(ItemStack stack, DataComponentType<?> type, Operation<Boolean> original) {
        return ForgeFurnaceFuel.isFuel(stack, original.call(stack, type), rose$recipeType());
    }

    private RecipeType<?> rose$recipeType() {
        Object self = this;
        if (self instanceof BlastFurnaceBlockEntity) return RecipeType.BLASTING;
        if (self instanceof SmokerBlockEntity) return RecipeType.SMOKING;
        return RecipeType.SMELTING;
    }
}

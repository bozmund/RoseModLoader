package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeAttributes;

/** Entity types get the default attributes Forge mods gave them (ForgeAttributes) before vanilla's. */
@Mixin(DefaultAttributes.class)
public abstract class DefaultAttributesMixin {
    @Inject(method = "getSupplier", at = @At("HEAD"), cancellable = true)
    private static void rose$forgeSupplier(EntityType<? extends LivingEntity> type, CallbackInfoReturnable<AttributeSupplier> cir) {
        AttributeSupplier forge = ForgeAttributes.get(type);
        if (forge != null) cir.setReturnValue(forge);
    }

    @Inject(method = "hasSupplier", at = @At("HEAD"), cancellable = true)
    private static void rose$forgeHasSupplier(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeAttributes.get(type) != null) cir.setReturnValue(true);
    }
}

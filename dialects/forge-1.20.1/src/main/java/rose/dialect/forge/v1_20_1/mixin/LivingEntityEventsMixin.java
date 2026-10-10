package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.LivingEvents;

/**
 * Forge 1.20.1 living events, fired where Forge's patches fired them: LivingHurtEvent before armor (ForgeHooks
 * .onLivingHurt in actuallyHurt), LivingDamageEvent after armor and absorption (onLivingDamage), LivingKnockBackEvent
 * before knockback (Player has its own actuallyHurt: PlayerEventsMixin), LivingEntityUseItemEvent.Finish when an item is used up.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin {
    @Unique
    private static final ThreadLocal<Boolean> rose$knockbackPosted = ThreadLocal.withInitial(() -> false);

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float rose$livingHurt(float damage, ServerLevel level, DamageSource source) {
        return LivingEvents.hurt((LivingEntity) (Object) this, level, source, damage);
    }

    @ModifyVariable(method = "actuallyHurt", argsOnly = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0, shift = At.Shift.AFTER))
    private float rose$livingDamage(float damage, ServerLevel level, DamageSource source) {
        return LivingEvents.damage((LivingEntity) (Object) this, source, damage);
    }

    @Inject(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V", at = @At("HEAD"), cancellable = true)
    private void rose$knockBack(double power, double xd, double zd, DamageSource source, float damage, boolean fromEffect, CallbackInfo ci) {
        if (!ForgeDialect.active() || rose$knockbackPosted.get()) return;
        LivingEntity self = (LivingEntity) (Object) this;
        LivingKnockBackEvent event = new LivingKnockBackEvent(self, (float) power, xd, zd);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();
            return;
        }
        if (event.getStrength() != (float) power || event.getRatioX() != xd || event.getRatioZ() != zd) {
            ci.cancel();
            rose$knockbackPosted.set(true);
            try {
                self.knockback(event.getStrength(), event.getRatioX(), event.getRatioZ(), source, damage, fromEffect);
            } finally {
                rose$knockbackPosted.set(false);
            }
        }
    }

    @WrapOperation(method = "completeUsingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack rose$useItemFinish(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
        if (!ForgeDialect.active()) return original.call(stack, level, entity);
        ItemStack used = stack.copy();
        ItemStack result = original.call(stack, level, entity);
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, used, entity.getUseItemRemainingTicks(), result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResultStack();
    }
}

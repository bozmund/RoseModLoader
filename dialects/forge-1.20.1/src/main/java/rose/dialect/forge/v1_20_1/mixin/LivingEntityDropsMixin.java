package rose.dialect.forge.v1_20_1.mixin;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.DropCapture;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/**
 * Forge 1.20.1 LivingDropsEvent (death drops are held back, then spawned unless canceled) and
 * LivingEquipmentChangeEvent (each slot that changed since the last tick).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {
    @Shadow
    protected int lastHurtByPlayerMemoryTime;

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void rose$captureDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if (ForgeDialect.active()) DropCapture.start((LivingEntity) (Object) this);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void rose$livingDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if (!ForgeDialect.active()) return;
        List<ItemEntity> drops = DropCapture.finish();
        LivingDropsEvent event = new LivingDropsEvent((LivingEntity) (Object) this, source, drops, 0, lastHurtByPlayerMemoryTime > 0);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        for (ItemEntity drop : event.getDrops()) level.addFreshEntity(drop);
    }

    @Inject(method = "collectEquipmentChanges", at = @At("RETURN"))
    private void rose$equipmentChanged(java.util.Map<EquipmentSlot, ItemStack> last, CallbackInfoReturnable<java.util.Map<EquipmentSlot, ItemStack>> cir) {
        if (!ForgeDialect.active() || cir.getReturnValue() == null) return;
        LivingEntity self = (LivingEntity) (Object) this;
        cir.getReturnValue().forEach((slot, to) -> MinecraftForge.EVENT_BUS.post(
                new LivingEquipmentChangeEvent(self, slot, last.getOrDefault(slot, ItemStack.EMPTY), to)));
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.LivingEvents;

/**
 * Forge 1.20.1 player events: EntityInteract before a player interacts with an entity (both sides, as
 * ForgeHooks.onInteractEntity), and the hurt/damage events in Player's own actuallyHurt.
 */
@Mixin(Player.class)
public abstract class PlayerEventsMixin {
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void rose$entityInteract(Entity target, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (!ForgeDialect.active()) return;
        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract((Player) (Object) this, hand, target);
        if (MinecraftForge.EVENT_BUS.post(event)) cir.setReturnValue(event.getCancellationResult());
    }

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float rose$livingHurt(float damage, ServerLevel level, DamageSource source) {
        return LivingEvents.hurt((Player) (Object) this, level, source, damage);
    }

    @ModifyVariable(method = "actuallyHurt", argsOnly = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V", ordinal = 0, shift = At.Shift.AFTER))
    private float rose$livingDamage(float damage, ServerLevel level, DamageSource source) {
        return LivingEvents.damage((Player) (Object) this, source, damage);
    }
}

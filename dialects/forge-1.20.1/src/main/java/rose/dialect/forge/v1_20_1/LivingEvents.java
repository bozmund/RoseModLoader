package rose.dialect.forge.v1_20_1;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Forge 1.20.1's damage hooks (ForgeHooks.onLivingHurt / onLivingDamage), shared by LivingEntity and Player, which
 * both have their own actuallyHurt.
 */
public final class LivingEvents {
    private LivingEvents() {
    }

    /** LivingHurtEvent, before armor. A canceled hurt deals no damage: vanilla skips everything for 0 damage. */
    public static float hurt(LivingEntity entity, ServerLevel level, DamageSource source, float damage) {
        if (!ForgeDialect.active() || entity.isInvulnerableTo(level, source)) return damage;
        LivingHurtEvent event = new LivingHurtEvent(entity, source, damage);
        return MinecraftForge.EVENT_BUS.post(event) ? 0.0F : Math.max(event.getAmount(), 0.0F);
    }

    /** LivingDamageEvent, after armor and absorption, right before health is reduced. */
    public static float damage(LivingEntity entity, DamageSource source, float damage) {
        if (!ForgeDialect.active() || damage == 0.0F) return damage;
        LivingDamageEvent event = new LivingDamageEvent(entity, source, damage);
        return MinecraftForge.EVENT_BUS.post(event) ? 0.0F : event.getAmount();
    }
}

package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

/** Bridge helpers for mob effects (rules/forge-1.20.1/bridges.tsv). */
public final class MobEffectBridges {
    /**
     * 1.20.1 {@code applyEffectTick(entity, amplifier)}; 26.3 passes the server level and asks whether the effect
     * stays (1.20.1 effects never ended themselves this way).
     */
    public static boolean applyEffectTick(MobEffect self, ServerLevel level, LivingEntity entity, int amplifier) {
        var old = Legacy.require(self, "applyEffectTick", MethodType.methodType(void.class, LivingEntity.class, int.class));
        Legacy.invoke(old, self, entity, amplifier);
        return true;
    }

    private MobEffectBridges() {}
}

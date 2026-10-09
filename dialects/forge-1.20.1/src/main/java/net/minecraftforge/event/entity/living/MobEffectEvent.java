package net.minecraftforge.event.entity.living;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

public class MobEffectEvent extends LivingEvent {
    protected final MobEffectInstance effectInstance;

    public MobEffectEvent(LivingEntity living, MobEffectInstance effectInstance) {
        super(living);
        this.effectInstance = effectInstance;
    }

    public MobEffectInstance getEffectInstance() {
        return effectInstance;
    }

    /** An effect is being removed (e.g. by milk). Canceling keeps it. */
    @Cancelable
    public static class Remove extends MobEffectEvent {
        private final Holder<MobEffect> effect;

        public Remove(LivingEntity living, MobEffectInstance effectInstance) {
            super(living, effectInstance);
            this.effect = effectInstance.getEffect();
        }

        public Remove(LivingEntity living, Holder<MobEffect> effect) {
            super(living, living.getEffect(effect));
            this.effect = effect;
        }

        public MobEffect getEffect() {
            return effect.value();
        }
    }

    @HasResult
    public static class Applicable extends MobEffectEvent {
        public Applicable(LivingEntity living, MobEffectInstance effectInstance) {
            super(living, effectInstance);
        }
    }

    public static class Added extends MobEffectEvent {
        private final MobEffectInstance oldEffectInstance;

        public Added(LivingEntity living, MobEffectInstance oldEffectInstance, MobEffectInstance newEffectInstance) {
            super(living, newEffectInstance);
            this.oldEffectInstance = oldEffectInstance;
        }

        public MobEffectInstance getOldEffectInstance() {
            return oldEffectInstance;
        }
    }

    public static class Expired extends MobEffectEvent {
        public Expired(LivingEntity living, MobEffectInstance effectInstance) {
            super(living, effectInstance);
        }
    }
}

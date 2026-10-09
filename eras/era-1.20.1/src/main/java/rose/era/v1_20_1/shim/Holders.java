package rose.era.v1_20_1.shim;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Conversions between registry values and holders (rules/forge-1.20.1/conversions.tsv). 1.20.5 changed many APIs
 * from {@code MobEffect} to {@code Holder<MobEffect>} (likewise attributes, potions, sounds); a 1.20.1 mod passes and
 * expects the plain value.
 */
public final class Holders {
    public static Holder<MobEffect> mobEffect(MobEffect value) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(value);
    }

    public static Holder<Attribute> attribute(Attribute value) {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(value);
    }

    public static Holder<Potion> potion(Potion value) {
        return BuiltInRegistries.POTION.wrapAsHolder(value);
    }

    public static Holder<SoundEvent> soundEvent(SoundEvent value) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(value);
    }

    public static Holder<GameEvent> gameEvent(GameEvent value) {
        return BuiltInRegistries.GAME_EVENT.wrapAsHolder(value);
    }

    /** {@code Holder<T>} to {@code T} (the caller casts). */
    public static Object value(Holder<?> holder) {
        return holder == null ? null : rose.era.v1_20_1.PreFreeze.value(holder);
    }

    /** {@code Holder.Reference<T>} to {@code T}: fields such as {@code GameEvent.BLOCK_CHANGE} are references. */
    public static Object referenceValue(Holder.Reference<?> holder) {
        return value(holder);
    }

    private Holders() {}
}

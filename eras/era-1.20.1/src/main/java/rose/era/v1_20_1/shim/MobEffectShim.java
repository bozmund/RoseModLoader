package rose.era.v1_20_1.shim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Redirect targets for 1.20.1 mob effect members that tooltips and effects use (changed in 1.20.5 and later). */
public final class MobEffectShim {
    /**
     * 1.20.1 {@code getAttributeModifiers()}: each attribute with the effect's modifier at amplifier 0. 26.3 keeps
     * templates and hands out modifiers per amplifier ({@code createModifiers}).
     */
    public static Map<Attribute, AttributeModifier> getAttributeModifiers(MobEffect self) {
        Map<Attribute, AttributeModifier> out = new LinkedHashMap<>();
        self.createModifiers(0, (attribute, modifier) -> out.put(attribute.value(), modifier));
        return out;
    }

    /**
     * 1.20.1 {@code getAttributeModifierValue(amplifier, modifier)} = amount * (amplifier + 1), or a mod effect's own
     * override of it.
     */
    public static double getAttributeModifierValue(MobEffect self, int amplifier, AttributeModifier modifier) {
        try {
            Method override = self.getClass().getMethod("getAttributeModifierValue", int.class, AttributeModifier.class);
            return (Double) override.invoke(self, amplifier, modifier);
        } catch (NoSuchMethodException e) {
            return modifier.amount() * (amplifier + 1);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 1.20.1 {@code MobEffectUtil.formatDuration(effect, scale)} counted 20 ticks a second; 26.3 takes the tick rate. */
    public static Component formatDuration(MobEffectInstance effect, float scale) {
        return MobEffectUtil.formatDuration(effect, scale, 20.0F);
    }

    /** 1.20.1 {@code ItemStack.ATTRIBUTE_MODIFIER_FORMAT}; 26.3 keeps it in ItemAttributeModifiers. */
    public static DecimalFormat attributeModifierFormat() {
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;
    }

    private MobEffectShim() {}
}

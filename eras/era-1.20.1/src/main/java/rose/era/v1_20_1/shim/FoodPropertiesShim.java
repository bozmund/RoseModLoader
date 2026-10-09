package rose.era.v1_20_1.shim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Redirect targets for 1.20.1 food properties. 1.20.5 split food: FoodProperties keeps nutrition and saturation,
 * while eating time and effects moved to the Consumable component. A 1.20.1 builder's extras (effects, "fast",
 * "meat") are kept on the side and become the item's Consumable when the item is given this food.
 */
public final class FoodPropertiesShim {
    /** Extras of one builder or one built FoodProperties. */
    private static final class Extras {
        final List<Supplier<MobEffectInstance>> effects = new ArrayList<>();
        final List<Float> chances = new ArrayList<>();
        boolean fast;
        boolean meat;
    }

    private static final Map<Object, Extras> EXTRAS = Collections.synchronizedMap(new WeakHashMap<>());

    private static Extras extras(Object key) {
        return EXTRAS.computeIfAbsent(key, k -> new Extras());
    }

    /** 1.20.1 {@code effect(MobEffectInstance, float)}. */
    public static FoodProperties.Builder effect(FoodProperties.Builder self, MobEffectInstance effect, float chance) {
        return effect(self, () -> effect, chance);
    }

    /** Forge 1.20.1 {@code effect(Supplier<MobEffectInstance>, float)}: resolved when the item's components are built. */
    public static FoodProperties.Builder effect(FoodProperties.Builder self, Supplier<MobEffectInstance> effect, float chance) {
        Extras e = extras(self);
        e.effects.add(effect);
        e.chances.add(chance);
        return self;
    }

    /** 1.20.1 {@code fast()}: eaten in half the time (Consumable consume_seconds 0.8). */
    public static FoodProperties.Builder fast(FoodProperties.Builder self) {
        extras(self).fast = true;
        return self;
    }

    /** 1.20.1 {@code meat()}: wolves eat it. 26.3 uses the #minecraft:meat item tag instead (data). */
    public static FoodProperties.Builder meat(FoodProperties.Builder self) {
        extras(self).meat = true;
        return self;
    }

    public static FoodProperties build(FoodProperties.Builder self) {
        FoodProperties built = self.build();
        Extras e = EXTRAS.remove(self);
        if (e != null) EXTRAS.put(built, e);
        return built;
    }

    /** 1.20.1 {@code isMeat()}. */
    public static boolean isMeat(FoodProperties self) {
        Extras e = EXTRAS.get(self);
        return e != null && e.meat;
    }

    /** 1.20.1 {@code getEffects()}: the effects with their chances (suppliers resolved now). */
    public static List<com.mojang.datafixers.util.Pair<MobEffectInstance, Float>> getEffects(FoodProperties self) {
        Extras e = EXTRAS.get(self);
        List<com.mojang.datafixers.util.Pair<MobEffectInstance, Float>> out = new ArrayList<>();
        if (e == null) return out;
        for (int i = 0; i < e.effects.size(); i++) out.add(com.mojang.datafixers.util.Pair.of(e.effects.get(i).get(), e.chances.get(i)));
        return out;
    }

    /** 1.20.1 {@code Item.Properties.food(food)}: the food component plus a Consumable carrying the 1.20.1 extras. */
    public static Item.Properties food(Item.Properties self, FoodProperties food) {
        Extras e = EXTRAS.get(food);
        if (e == null || (e.effects.isEmpty() && !e.fast)) return self.food(food);
        // Effects may point at mod effects registered after items, so build the Consumable later.
        return self.component(DataComponents.FOOD, food).delayedComponent(DataComponents.CONSUMABLE, context -> {
            Consumable.Builder consumable = Consumables.defaultFood();
            if (e.fast) consumable.consumeSeconds(0.8F);
            for (int i = 0; i < e.effects.size(); i++) {
                consumable.onConsume(new ApplyStatusEffectsConsumeEffect(e.effects.get(i).get(), e.chances.get(i)));
            }
            return consumable.build();
        });
    }

    private FoodPropertiesShim() {}
}

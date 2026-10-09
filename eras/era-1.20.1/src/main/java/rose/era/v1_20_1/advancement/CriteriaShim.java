package rose.era.v1_20_1.advancement;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import rose.era.v1_20_1.LateRegistration;

/** 1.20.1 {@code CriteriaTriggers.register(trigger)}: registered under the trigger's own id. */
public final class CriteriaShim {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends CriterionTrigger<?>> T register(T trigger) {
        if (!(trigger instanceof LegacySimpleCriterionTrigger<?> legacy)) {
            throw new IllegalArgumentException("A 1.20.1 trigger must know its id: " + trigger.getClass().getName());
        }
        ResourceKey key = ResourceKey.create(Registries.TRIGGER_TYPE, legacy.getId());
        if (!BuiltInRegistries.TRIGGER_TYPES.containsKey(key)) {
            LateRegistration.register((MappedRegistry) BuiltInRegistries.TRIGGER_TYPES, key, trigger);
        }
        return trigger;
    }

    private CriteriaShim() {}
}

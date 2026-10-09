package rose.era.v1_20_1.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;

/**
 * 1.20.1 {@code SimpleCriterionTrigger}: knew its id and built instances from the criterion's JSON conditions.
 * 1.20.5 triggers are registered by id and parse instances with a codec; this codec hands the conditions to the old
 * {@code createInstance}. Mod triggers are rebased onto this class.
 */
public abstract class LegacySimpleCriterionTrigger<T extends AbstractCriterionTriggerInstance> extends SimpleCriterionTrigger<T> {
    private final Codec<T> codec = Codec.PASSTHROUGH.comapFlatMap(this::parse,
            instance -> new Dynamic<>(JsonOps.INSTANCE, new JsonObject()));

    public abstract Identifier getId();

    protected abstract T createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context);

    @Override
    public Codec<T> codec() {
        return codec;
    }

    private DataResult<T> parse(Dynamic<?> conditions) {
        JsonElement json = conditions.convert(JsonOps.INSTANCE).getValue();
        try {
            return DataResult.success(createInstance(json.isJsonObject() ? json.getAsJsonObject() : new JsonObject(), ContextAwarePredicate.ANY, null));
        } catch (RuntimeException e) {
            return DataResult.error(() -> getId() + ": " + e.getMessage());
        }
    }
}

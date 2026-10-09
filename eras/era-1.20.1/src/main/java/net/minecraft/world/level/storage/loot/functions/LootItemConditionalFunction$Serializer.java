package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Era bridge (1.20.1): base serializer of conditional loot functions; reads {@code "conditions"} and passes them on.
 * (A top-level class named like the old nested one, which 26.3 doesn't have.)
 */
public abstract class LootItemConditionalFunction$Serializer<T extends LootItemConditionalFunction> implements Serializer<T> {
    @Override
    public void serialize(JsonObject json, T value, JsonSerializationContext context) {
        throw new UnsupportedOperationException("Loot functions from 1.20.1 mods are only read, not written");
    }

    @Override
    public final T deserialize(JsonObject json, JsonDeserializationContext context) {
        LootItemCondition[] conditions = json.has("conditions")
                ? context.deserialize(json.get("conditions"), LootItemCondition[].class)
                : new LootItemCondition[0];
        return deserialize(json, context, conditions);
    }

    public abstract T deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions);
}

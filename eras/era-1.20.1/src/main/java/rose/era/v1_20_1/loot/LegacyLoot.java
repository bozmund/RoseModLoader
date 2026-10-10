package rose.era.v1_20_1.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Adapts 1.20.1 JSON loot serializers to 26.3 MapCodecs. A function encodes as the JSON it was read from (26.3
 * encodes loot tables, e.g. for commands and data generation; the old serializers wrote 1.20.1 JSON anyway).
 */
public final class LegacyLoot {
    private static final Map<LootItemFunctionType, MapCodec<?>> CODECS = Collections.synchronizedMap(new IdentityHashMap<>());
    /** The JSON each function was read from, by identity. */
    private static final Map<LootItemFunction, JsonObject> SOURCES = new com.google.common.collect.MapMaker().weakKeys().makeMap();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static MapCodec<? extends LootItemFunction> adapt(LootItemFunctionType type) {
        return (MapCodec) CODECS.computeIfAbsent(type, t -> codec(t));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static MapCodec codecFor(LootItemFunctionType type) {
        MapCodec<?> codec = CODECS.get(type);
        if (codec == null) throw new IllegalStateException("Loot function type " + type + " was never registered");
        return codec;
    }

    private static MapCodec<LootItemFunction> codec(LootItemFunctionType type) {
        return new MapCodec<>() {
            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.empty();
            }

            @Override
            public <O> DataResult<LootItemFunction> decode(DynamicOps<O> ops, MapLike<O> input) {
                JsonObject json = new JsonObject();
                input.entries().forEach(e -> ops.getStringValue(e.getFirst())
                        .ifSuccess(key -> json.add(key, ops.convertTo(JsonOps.INSTANCE, e.getSecond()))));
                DynamicOps<JsonElement> jsonOps = ops instanceof RegistryOps<?> registryOps ? registryOps.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE;
                try {
                    LootItemFunction function = type.getSerializer().deserialize(json, context(jsonOps));
                    SOURCES.put(function, json);
                    return DataResult.success(function);
                } catch (RuntimeException | LinkageError e) {
                    return DataResult.error(() -> "Legacy loot function " + type.getSerializer().getClass().getName() + " failed: " + e);
                }
            }

            @Override
            public <O> RecordBuilder<O> encode(LootItemFunction value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                JsonObject json = SOURCES.get(value);
                if (json == null) return prefix.withErrorsFrom(DataResult.error(() -> "This 1.20.1 loot function wasn't read from JSON"));
                return encodeJson(json, "function", ops, prefix);
            }
        };
    }

    /** The entries of a 1.20.1 JSON object, except the dispatch key (the 26.3 codec writes it). */
    public static <O> RecordBuilder<O> encodeJson(JsonObject json, String dispatchKey, DynamicOps<O> ops, RecordBuilder<O> prefix) {
        for (var entry : json.entrySet()) {
            if (!entry.getKey().equals(dispatchKey)) prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
        }
        return prefix;
    }

    /** Gson's deserialization context as 1.20.1 loot serializers used it: for nested conditions. */
    private static JsonDeserializationContext context(DynamicOps<JsonElement> ops) {
        return new JsonDeserializationContext() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T deserialize(JsonElement json, Type type) throws JsonParseException {
                if (type == LootItemCondition[].class) {
                    return (T) LootItemCondition.DIRECT_CODEC.listOf().parse(ops, json).getOrThrow(JsonParseException::new)
                            .toArray(LootItemCondition[]::new);
                }
                if (type == LootItemCondition.class) {
                    return (T) LootItemCondition.DIRECT_CODEC.parse(ops, json).getOrThrow(JsonParseException::new);
                }
                throw new JsonParseException("Rose can't read " + type + " for a 1.20.1 loot serializer yet");
            }
        };
    }

    private LegacyLoot() {}
}

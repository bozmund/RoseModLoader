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

/** Adapts 1.20.1 JSON loot serializers to 26.3 MapCodecs. */
public final class LegacyLoot {
    private static final Map<LootItemFunctionType, MapCodec<?>> CODECS = Collections.synchronizedMap(new IdentityHashMap<>());

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
                    return DataResult.success(type.getSerializer().deserialize(json, context(jsonOps)));
                } catch (RuntimeException | LinkageError e) {
                    return DataResult.error(() -> "Legacy loot function " + type.getSerializer().getClass().getName() + " failed: " + e);
                }
            }

            @Override
            public <O> RecordBuilder<O> encode(LootItemFunction value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return prefix.withErrorsFrom(DataResult.error(() -> "Loot functions from 1.20.1 mods can't be encoded"));
            }
        };
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

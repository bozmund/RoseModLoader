package rose.era.v1_20_1.crafting;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Adapts 1.20.1 recipe serializers to 26.3 ones (a MapCodec that hands the JSON to the old fromJson). A recipe
 * encodes as the JSON it was read from: 1.20.1 serializers had no JSON writer.
 */
public final class LegacyRecipes {
    /** The JSON each recipe was read from, by identity (records may be equal and still be different recipes). */
    private static final Map<Recipe<?>, JsonObject> SOURCES = new com.google.common.collect.MapMaker().weakKeys().makeMap();
    private static final Identifier UNKNOWN = Identifier.fromNamespaceAndPath("rose", "unknown_recipe");
    private static final ThreadLocal<Identifier> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<DynamicOps<com.google.gson.JsonElement>> CURRENT_OPS = new ThreadLocal<>();
    private static final Map<LegacyRecipeSerializer<?>, RecipeSerializer<?>> ADAPTED = Collections.synchronizedMap(new IdentityHashMap<>());

    /** The 26.3 serializer for a legacy one, created once. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static RecipeSerializer<?> adapt(LegacyRecipeSerializer<?> legacy) {
        return ADAPTED.computeIfAbsent(legacy, l -> new RecipeSerializer(codec(l), streamCodec(l)));
    }

    /** The 26.3 serializer registered for {@code legacy}, or {@code null}. */
    public static RecipeSerializer<?> adapted(LegacyRecipeSerializer<?> legacy) {
        return ADAPTED.get(legacy);
    }

    /** Set by RecipeIdMixin while a recipe file is decoded: old serializers want the recipe's id. */
    public static void setCurrentId(Identifier id) {
        if (id == null) CURRENT_ID.remove();
        else CURRENT_ID.set(id);
    }

    /** The registry-aware JSON ops of the recipe being decoded (for old code that parses ingredients itself). */
    public static DynamicOps<com.google.gson.JsonElement> currentOps() {
        return CURRENT_OPS.get();
    }

    /**
     * Replaces deferred recipes in a recipe map with the mods' real recipe objects (recipes that fail to parse are
     * left out and logged). Called when the recipe manager finalizes loading: components and tags are bound by then.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static net.minecraft.world.item.crafting.RecipeMap materialize(net.minecraft.world.item.crafting.RecipeMap map,
                                                                          net.minecraft.core.HolderLookup.Provider registries) {
        var byKey = ((rose.era.v1_20_1.mixin.RecipeMapInvoker) (Object) map).rose$byKey();
        if (byKey.values().stream().noneMatch(h -> h.value() instanceof DeferredLegacyRecipe)) return map;
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        com.google.common.collect.ImmutableMultimap.Builder<net.minecraft.world.item.crafting.RecipeType<?>, net.minecraft.world.item.crafting.RecipeHolder<?>> newByType =
                com.google.common.collect.ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<net.minecraft.resources.ResourceKey<Recipe<?>>, net.minecraft.world.item.crafting.RecipeHolder<?>> newByKey =
                com.google.common.collect.ImmutableMap.builder();
        int made = 0;
        int failed = 0;
        for (var entry : byKey.entrySet()) {
            net.minecraft.world.item.crafting.RecipeHolder<?> holder = entry.getValue();
            if (holder.value() instanceof DeferredLegacyRecipe deferred) {
                CURRENT_ID.set(deferred.id);
                CURRENT_OPS.set(ops);
                try {
                    Recipe<?> real = deferred.serializer.fromJson(deferred.id, deferred.json);
                    SOURCES.put(real, deferred.json);
                    holder = new net.minecraft.world.item.crafting.RecipeHolder(entry.getKey(), real);
                    made++;
                } catch (RuntimeException | LinkageError e) {
                    failed++;
                    com.mojang.logging.LogUtils.getLogger().warn("[rose] skipped 1.20.1 recipe {}: {}", deferred.id, e.toString());
                    continue;
                } finally {
                    CURRENT_ID.remove();
                    CURRENT_OPS.remove();
                }
            }
            newByType.put(holder.value().getType(), holder);
            newByKey.put(entry.getKey(), holder);
        }
        com.mojang.logging.LogUtils.getLogger().info("[rose] created {} recipes from 1.20.1 mods ({} skipped)", made, failed);
        return rose.era.v1_20_1.mixin.RecipeMapInvoker.rose$create(newByType.build(), newByKey.build());
    }

    private static Identifier currentId() {
        Identifier id = CURRENT_ID.get();
        return id != null ? id : UNKNOWN;
    }

    private static <T extends Recipe<?>> MapCodec<T> codec(LegacyRecipeSerializer<T> legacy) {
        return new MapCodec<>() {
            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.empty();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                JsonObject json = new JsonObject();
                input.entries().forEach(entry -> ops.getStringValue(entry.getFirst())
                        .ifSuccess(key -> json.add(key, ops.convertTo(JsonOps.INSTANCE, entry.getSecond()))));
                // Parsed later: old serializers create item stacks, which need item components (see DeferredLegacyRecipe).
                return DataResult.success((T) (Object) new DeferredLegacyRecipe(legacy, currentId(), json));
            }

            @Override
            public <O> RecordBuilder<O> encode(T value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                JsonObject json = value instanceof DeferredLegacyRecipe deferred ? deferred.json : SOURCES.get(value);
                if (json == null) return prefix.withErrorsFrom(DataResult.error(() -> "This 1.20.1 recipe wasn't read from JSON"));
                return rose.era.v1_20_1.loot.LegacyLoot.encodeJson(json, "type", ops, prefix);
            }
        };
    }

    private static <T extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(LegacyRecipeSerializer<T> legacy) {
        return StreamCodec.of((buf, recipe) -> legacy.toNetwork(buf, recipe), buf -> legacy.fromNetwork(currentId(), buf));
    }

    private LegacyRecipes() {}
}

package rose.era.v1_20_1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import rose.era.v1_20_1.crafting.LegacyRecipeSerializer;
import rose.era.v1_20_1.loot.LegacyLoot;
import rose.era.v1_20_1.crafting.LegacyRecipes;
import rose.era.v1_20_1.worldgen.LegacyFeature;
import rose.era.v1_20_1.worldgen.LegacyPlacement;
import rose.era.v1_20_1.worldgen.LegacyPlacementModifier;

/**
 * Some 1.20.1 registry values have a different type on 26.3 (recipe serializers became codec records). A dialect
 * registers {@link #adapt adapted} values while the mod keeps using the object it created.
 */
public final class RegistryAdapters {
    /** The value to put in the 26.3 registry for a value a 1.20.1 mod registers. */
    public static Object adapt(Object value) {
        if (value instanceof LegacyRecipeSerializer<?> legacy) return LegacyRecipes.adapt(legacy);
        if (value instanceof PlacementModifierType<?> type) return placementType(type);
        if (value instanceof LegacyFeature<?> feature) return feature.typeCodec();
        if (value instanceof LootItemFunctionType type) return LegacyLoot.adapt(type);
        return value;
    }

    private static final Map<PlacementModifierType<?>, MapCodec<?>> PLACEMENT_TYPES = Collections.synchronizedMap(new IdentityHashMap<>());

    /**
     * A placement modifier type's MapCodec; 1.20.1 modifiers decode as {@link LegacyPlacement}s. One instance per type:
     * 26.3 finds a modifier's type by looking its codec up in the registry.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static MapCodec<?> placementType(PlacementModifierType<?> type) {
        return PLACEMENT_TYPES.computeIfAbsent(type, t -> {
            MapCodec<Object> base = mapCodec((Codec) t.codec());
            MapCodec<?>[] self = new MapCodec<?>[1];
            self[0] = base.xmap(o -> o instanceof LegacyPlacementModifier legacy ? new LegacyPlacement(legacy, (MapCodec) self[0]) : o,
                    o -> o instanceof LegacyPlacement placement ? placement.legacy() : o);
            return self[0];
        });
    }

    /** 26.3 registries of "types" hold MapCodecs; 1.20.1 types handed out Codecs (usually record codecs). */
    static <T> MapCodec<T> mapCodec(Codec<T> codec) {
        if (codec instanceof MapCodec.MapCodecCodec<T> wrapped) return wrapped.codec();
        return codec.fieldOf("value");
    }

    private RegistryAdapters() {}
}

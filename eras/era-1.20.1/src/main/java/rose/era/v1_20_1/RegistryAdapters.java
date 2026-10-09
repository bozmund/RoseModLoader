package rose.era.v1_20_1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import rose.era.v1_20_1.crafting.LegacyRecipeSerializer;
import rose.era.v1_20_1.loot.LegacyLoot;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/**
 * Some 1.20.1 registry values have a different type on 26.3 (recipe serializers became codec records). A dialect
 * registers {@link #adapt adapted} values while the mod keeps using the object it created.
 */
public final class RegistryAdapters {
    /** The value to put in the 26.3 registry for a value a 1.20.1 mod registers. */
    public static Object adapt(Object value) {
        if (value instanceof LegacyRecipeSerializer<?> legacy) return LegacyRecipes.adapt(legacy);
        if (value instanceof PlacementModifierType<?> type) return mapCodec(type.codec());
        if (value instanceof LootItemFunctionType type) return LegacyLoot.adapt(type);
        return value;
    }

    /** 26.3 registries of "types" hold MapCodecs; 1.20.1 types handed out Codecs (usually record codecs). */
    static <T> MapCodec<T> mapCodec(Codec<T> codec) {
        if (codec instanceof MapCodec.MapCodecCodec<T> wrapped) return wrapped.codec();
        return codec.fieldOf("value");
    }

    private RegistryAdapters() {}
}

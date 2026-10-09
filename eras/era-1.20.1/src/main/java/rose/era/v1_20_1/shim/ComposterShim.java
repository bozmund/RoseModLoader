package rose.era.v1_20_1.shim;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * 1.20.1 {@code ComposterBlock.COMPOSTABLES} (item -> chance to add a layer). 26.3 reads the item's
 * {@code minecraft:compostable} component instead, so each {@code put} adds a component initializer; it takes effect
 * when components are (re)built, i.e. on the next server start or reload.
 */
public final class ComposterShim {
    private static final Object2FloatMap<ItemLike> COMPOSTABLES = new Legacy();

    public static Object2FloatMap<ItemLike> COMPOSTABLES() {
        return COMPOSTABLES;
    }

    /** The vanilla chance tier nearest to a 1.20.1 chance (vanilla used 0.3, 0.5, 0.65, 0.85 and 1.0). */
    static ResourceKey<ContextIntProvider> tier(float chance) {
        if (chance < 0.4f) return ContextIntProviders.COMPOSTABLE_LOW;
        if (chance < 0.575f) return ContextIntProviders.COMPOSTABLE_LOW_MEDIUM;
        if (chance < 0.75f) return ContextIntProviders.COMPOSTABLE_MEDIUM;
        if (chance < 0.925f) return ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH;
        return ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE;
    }

    private static final class Legacy extends Object2FloatOpenHashMap<ItemLike> {
        @Override
        public float put(ItemLike like, float chance) {
            Item item = like.asItem();
            ResourceKey<Item> key = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
            if (key != null && chance > 0) {
                Compostable compostable = new Compostable(tier(chance));
                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(key, (components, context, k) -> components.set(DataComponents.COMPOSTABLE, compostable));
            }
            return super.put(item, chance);
        }

        @Override
        @Deprecated
        public Float put(ItemLike like, Float chance) {
            return put(like, chance.floatValue());
        }
    }

    private ComposterShim() {}
}

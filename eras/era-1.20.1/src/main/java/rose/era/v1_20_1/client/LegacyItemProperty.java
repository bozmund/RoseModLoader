package rose.era.v1_20_1.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * An item property a 1.20.1 mod registered ({@code ItemProperties.register}), as a 26.3 range property
 * ({@code rose:legacy_property}): packfix turns old model {@code overrides} into range_dispatch definitions over it.
 */
public record LegacyItemProperty(Identifier property) implements RangeSelectItemModelProperty {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("rose", "legacy_property");
    public static final MapCodec<LegacyItemProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("name").forGetter(LegacyItemProperty::property)).apply(i, LegacyItemProperty::new));

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        ItemPropertyFunction function = ItemProperties.getProperty(stack.getItem(), property);
        return function == null ? 0.0F : function.call(stack, level, owner == null ? null : owner.asLivingEntity(), seed);
    }

    @Override
    public MapCodec<LegacyItemProperty> type() {
        return MAP_CODEC;
    }
}

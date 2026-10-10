package rose.era.v1_20_1.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.client.LegacyItemProperty;

/** Registers {@code rose:legacy_property} with vanilla's range item model properties. */
@Mixin(RangeSelectItemModelProperties.class)
public abstract class RangeSelectItemModelPropertiesMixin {
    @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void rose$legacyProperty(CallbackInfo ci) {
        ID_MAPPER.put(LegacyItemProperty.ID, LegacyItemProperty.MAP_CODEC);
    }
}

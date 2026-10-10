package rose.era.v1_20_1.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.client.LegacyItemRenderer;

/** Registers the {@code rose:legacy_renderer} item model type. */
@Mixin(ItemModels.class)
public abstract class ItemModelsMixin {
    @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void rose$legacyRenderer(CallbackInfo ci) {
        ID_MAPPER.put(LegacyItemRenderer.ID, LegacyItemRenderer.Unbaked.MAP_CODEC);
    }
}

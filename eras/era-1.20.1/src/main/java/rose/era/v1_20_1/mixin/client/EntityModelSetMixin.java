package rose.era.v1_20_1.mixin.client;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.client.LegacyModelLayers;

/**
 * A layer vanilla doesn't define is looked up in the ones old mods registered (LegacyModelLayers). Looked up when
 * baked rather than added to the set: the set is built early in the resource reload, possibly before mods register.
 */
@Mixin(EntityModelSet.class)
public abstract class EntityModelSetMixin {
    @Shadow
    @Final
    private Map<ModelLayerLocation, LayerDefinition> roots;

    @Inject(method = "bakeLayer", at = @At("HEAD"), cancellable = true)
    private void rose$legacyLayer(ModelLayerLocation id, CallbackInfoReturnable<ModelPart> cir) {
        if (roots.containsKey(id)) return;
        Supplier<LayerDefinition> legacy = LegacyModelLayers.all().get(id);
        if (legacy != null) cir.setReturnValue(legacy.get().bakeRoot());
    }
}

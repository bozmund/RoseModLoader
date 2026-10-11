package rose.era.v1_20_1.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.client.KeyMappingShim;

/** A key category a 1.20.1 mod named by translation key shows that translation (see KeyMappingShim). */
@Mixin(KeyMapping.Category.class)
public abstract class KeyCategoryMixin {
    @Inject(method = "label", at = @At("HEAD"), cancellable = true)
    private void rose$legacyLabel(CallbackInfoReturnable<Component> cir) {
        String legacy = KeyMappingShim.LABELS.get((Object) this);
        if (legacy != null) cir.setReturnValue(Component.translatable(legacy));
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** FML's setup events come after registration is closed. */
@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("TAIL"))
    private static void rose$afterFreeze(CallbackInfo ci) {
        ForgeDialect.afterFreeze();
    }
}

package rose.core.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.core.RoseCore;
import rose.core.StateIds;

/**
 * Mods register content after vanilla's registries are filled but before they are frozen: the same moment on the
 * client, the dedicated server and the GameTest server (all call Bootstrap.bootStrap -> BuiltInRegistries.bootStrap).
 */
@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("HEAD"))
    private static void rose$beforeFreeze(CallbackInfo ci) {
        RoseCore.runMainEntrypoints();
        StateIds.assignMissing();
    }
}

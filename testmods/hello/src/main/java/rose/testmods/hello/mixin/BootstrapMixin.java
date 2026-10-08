package rose.testmods.hello.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.testmods.hello.HelloRose;

/** Runs on both sides: vanilla calls Bootstrap.bootStrap() early on the client and the dedicated server. */
@Mixin(targets = "net.minecraft.server.Bootstrap")
public abstract class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At("HEAD"))
    private static void hello$bootStrap(CallbackInfo ci) {
        HelloRose.mark("common/Bootstrap.bootStrap");
    }
}

package rose.testmods.hello.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.testmods.hello.HelloRose;

/**
 * Client only (listed under "client" in hello.mixins.json). Targets the client's constructor, which always runs,
 * unlike a specific screen (a fresh install shows an accessibility screen before the title screen).
 */
@Mixin(targets = "net.minecraft.client.Minecraft")
public abstract class MinecraftClientMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void hello$init(CallbackInfo ci) {
        HelloRose.mark("client/Minecraft.<init>");
    }
}

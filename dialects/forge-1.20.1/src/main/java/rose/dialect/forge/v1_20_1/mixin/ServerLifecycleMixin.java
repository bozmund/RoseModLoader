package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeServerEvents;

/** Forge's ServerAboutToStartEvent fires before the server loads its worlds. */
@Mixin(MinecraftServer.class)
public abstract class ServerLifecycleMixin {
    @Inject(method = "runServer", at = @At("HEAD"))
    private void rose$aboutToStart(CallbackInfo ci) {
        ForgeServerEvents.aboutToStart((MinecraftServer) (Object) this);
    }
}

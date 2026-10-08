package rose.core.mixin;

import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.api.event.RoseEvents;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    /** After initServer() succeeded and the status was built: the main loop starts next. */
    @Inject(method = "runServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;",
            shift = At.Shift.AFTER))
    private void rose$started(CallbackInfo ci) {
        RoseEvents.SERVER_STARTED.invoker().accept((MinecraftServer) (Object) this);
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void rose$tickEnd(BooleanSupplier haveTime, CallbackInfo ci) {
        RoseEvents.SERVER_TICK_END.invoker().accept((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void rose$stopping(CallbackInfo ci) {
        RoseEvents.SERVER_STOPPING.invoker().accept((MinecraftServer) (Object) this);
    }
}

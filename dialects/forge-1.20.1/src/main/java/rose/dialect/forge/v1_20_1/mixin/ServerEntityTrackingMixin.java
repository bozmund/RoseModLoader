package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 PlayerEvent.StartTracking / StopTracking, as a player's client starts or stops seeing an entity. */
@Mixin(ServerEntity.class)
public abstract class ServerEntityTrackingMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void rose$startTracking(ServerPlayer player, CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerEvent.StartTracking(player, entity));
    }

    @Inject(method = "removePairing", at = @At("TAIL"))
    private void rose$stopTracking(ServerPlayer player, CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerEvent.StopTracking(player, entity));
    }
}

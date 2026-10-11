package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 server player events: changed dimension, respawn point set. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEventsMixin {
    @Inject(method = "triggerDimensionChangeTriggers", at = @At("HEAD"))
    private void rose$changedDimension(ServerLevel oldLevel, CallbackInfo ci) {
        if (!ForgeDialect.active()) return;
        ServerPlayer self = (ServerPlayer) (Object) this;
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerChangedDimensionEvent(self, oldLevel.dimension(), self.level().dimension()));
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void rose$setSpawn(ServerPlayer.RespawnConfig config, boolean showMessage, CallbackInfo ci) {
        if (!ForgeDialect.active() || config == null) return;
        var pos = config.respawnData().globalPos();
        if (MinecraftForge.EVENT_BUS.post(new PlayerSetSpawnEvent((ServerPlayer) (Object) this, pos.dimension(), pos.pos(), config.forced()))) ci.cancel();
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 player list events: data sync, logged in, logged out, respawn (where Forge's PlayerList patch fired them). */
@Mixin(PlayerList.class)
public abstract class PlayerListEventsMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void rose$loggedIn(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (!ForgeDialect.active()) return;
        MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, player));
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(player));
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void rose$loggedOut(ServerPlayer player, CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    private void rose$respawned(ServerPlayer old, boolean keepAllPlayerData, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(cir.getReturnValue(), keepAllPlayerData));
    }

    @Inject(method = "reloadResources", at = @At("TAIL"))
    private void rose$datapackSync(CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, null));
    }
}

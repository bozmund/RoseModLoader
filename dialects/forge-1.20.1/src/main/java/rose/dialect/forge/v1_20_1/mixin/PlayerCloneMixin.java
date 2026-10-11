package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 PlayerEvent.Clone, once the new player copied the old one (ForgeEventFactory.onPlayerClone in restoreFrom). */
@Mixin(ServerPlayer.class)
public abstract class PlayerCloneMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void rose$clone(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone((ServerPlayer) (Object) this, oldPlayer, !restoreAll));
    }
}

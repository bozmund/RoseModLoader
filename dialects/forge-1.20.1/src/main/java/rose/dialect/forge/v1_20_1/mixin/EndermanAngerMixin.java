package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderManAngerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 EnderManAngerEvent: a canceled event means the player's stare doesn't count. */
@Mixin(Enderman.class)
public abstract class EndermanAngerMixin {
    @Inject(method = "isBeingStaredBy", at = @At("RETURN"), cancellable = true)
    private void rose$anger(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !ForgeDialect.active()) return;
        if (MinecraftForge.EVENT_BUS.post(new EnderManAngerEvent((Enderman) (Object) this, player))) cir.setReturnValue(false);
    }
}

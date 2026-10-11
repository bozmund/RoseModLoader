package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 PlayerXpEvent.PickupXp, before a player takes an orb (canceling leaves it). */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEventsMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void rose$pickupXp(Player player, CallbackInfo ci) {
        if (!ForgeDialect.active() || player.level().isClientSide() || player.takeXpDelay != 0) return;
        if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, (ExperienceOrb) (Object) this))) ci.cancel();
    }
}

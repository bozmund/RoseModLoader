package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialect;
import rose.dialect.forge.v1_20_1.SleepEvents;

/** Forge 1.20.1: a sleeping player stays in bed while SleepingTimeCheckEvent allows it; PlayerWakeUpEvent on getting up. */
@Mixin(Player.class)
public abstract class PlayerSleepMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
    private boolean rose$stillSleepingTime(BedRule rule, Level level, Operation<Boolean> original) {
        Player self = (Player) (Object) this;
        return SleepEvents.sleepingTime(self, self.getSleepingPos(), original.call(rule, level));
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void rose$wakeUp(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        if (ForgeDialect.active()) MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent((Player) (Object) this, forcefulWakeUp, updateLevelList));
    }
}

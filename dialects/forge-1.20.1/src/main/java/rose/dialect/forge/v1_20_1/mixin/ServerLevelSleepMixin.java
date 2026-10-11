package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/**
 * Forge 1.20.1 SleepFinishedTimeEvent: once everyone slept, the time the level wakes at (26.3 moves the level's clock
 * to its wake-up marker); a listener's later time is applied to the clock.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/clock/ServerClockManager$MoveResult;"))
    private ServerClockManager.MoveResult rose$sleepFinished(ServerClockManager clocks, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> marker,
                                                            Operation<ServerClockManager.MoveResult> original) {
        long before = clocks.getInstance(clock).totalTicks();
        ServerClockManager.MoveResult result = original.call(clocks, clock, marker);
        if (!ForgeDialect.active()) return result;
        long woke = clocks.getInstance(clock).totalTicks();
        SleepFinishedTimeEvent event = new SleepFinishedTimeEvent((ServerLevel) (Object) this, woke, before);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getNewTime() != woke) clocks.setTotalTicks(clock, event.getNewTime());
        return result;
    }
}

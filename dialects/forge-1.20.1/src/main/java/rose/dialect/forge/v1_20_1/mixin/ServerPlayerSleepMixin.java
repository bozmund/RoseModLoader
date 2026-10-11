package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.SleepEvents;

/** Forge 1.20.1 PlayerSleepInBedEvent and SleepingTimeCheckEvent when a server player gets into bed. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSleepMixin {
    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void rose$sleepInBed(AbstractBedBlock bed, BlockState state, BedRule rule, BlockPos pos,
                                 CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Player.BedSleepingProblem problem = SleepEvents.sleepInBed((Player) (Object) this, Optional.of(pos));
        if (problem != null) cir.setReturnValue(Either.left(problem));
    }

    @WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
    private boolean rose$sleepingTime(BedRule rule, Level level, Operation<Boolean> original, AbstractBedBlock bed, BlockState state,
                                      BedRule bedRule, BlockPos pos) {
        return SleepEvents.sleepingTime((Player) (Object) this, Optional.of(pos), original.call(rule, level));
    }
}

package rose.era.v1_20_1.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.bridge.BlockBridges;

/**
 * 1.20.1's Entity.move let the block an entity landed on (or bumped from below) change its motion
 * ({@code Block.updateEntityAfterFallOn}: slime and FD's safety net bounce). 26.x computes bounces from the block's
 * restitution instead; a mod block that overrides the old hook gets the motion it had before that and decides.
 */
@Mixin(Entity.class)
public abstract class EntityFallOnMixin {
    @Shadow public boolean verticalCollision;

    @Unique
    private Vec3 rose$motionBeforeCollision;

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vec3 movement);

    @Shadow
    public abstract net.minecraft.world.level.Level level();

    @Inject(method = "restituteMovementAfterCollisions", at = @At("HEAD"))
    private void rose$rememberMotion(BlockState effectState, boolean xCollision, boolean zCollision, Vec3 movement, CallbackInfo ci) {
        rose$motionBeforeCollision = getDeltaMovement();
    }

    @Inject(method = "restituteMovementAfterCollisions", at = @At("TAIL"))
    private void rose$legacyAfterFallOn(BlockState effectState, boolean xCollision, boolean zCollision, Vec3 movement, CallbackInfo ci) {
        if (!verticalCollision || !BlockBridges.hasAfterFallOn(effectState.getBlock())) return;
        setDeltaMovement(rose$motionBeforeCollision);
        BlockBridges.afterFallOn(effectState.getBlock(), level(), (Entity) (Object) this);
    }
}

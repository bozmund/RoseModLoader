package rose.era.v1_20_1.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.20.1 mods may build a block entity on a vanilla super constructor (FD's canvas sign: SignBlockEntity(pos, state),
 * so the sign type) and override {@code getType()} with their own type. 1.20.1 never checked the type against the
 * block; 26.x does, in the constructor, with the type the constructor was given. The overriding type is the one the
 * mod means (and the one 1.20.1 saved, synced and rendered by), so it replaces the given one before the check.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityTypeMixin {
    @Shadow @Final @Mutable private BlockEntityType<?> type;

    @Shadow
    public abstract BlockEntityType<?> getType();

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;type:Lnet/minecraft/world/level/block/entity/BlockEntityType;", shift = At.Shift.AFTER))
    private void rose$overriddenType(BlockEntityType<?> given, BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockEntityType<?> declared = getType();
        if (declared != null && declared != type) type = declared;
    }
}

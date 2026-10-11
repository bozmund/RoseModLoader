package rose.era.v1_20_1.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.block.LegacyBlockEntities;

/** Old chunks' mod block entities that extend vanilla ones are upgraded like them (see LegacyBlockEntities). */
@Mixin(SimpleRegionStorage.class)
public abstract class SimpleRegionStorageMixin {
    @Shadow @Final private DataFixTypes dataFixType;

    @Inject(method = "upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"))
    private void rose$asVanilla(CompoundTag chunk, int defaultVersion, CompoundTag context, int target, CallbackInfoReturnable<CompoundTag> cir) {
        if (dataFixType == DataFixTypes.CHUNK && NbtUtils.getDataVersion(chunk, defaultVersion) < target) LegacyBlockEntities.asVanilla(chunk);
    }

    @Inject(method = "upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void rose$restore(CompoundTag chunk, int defaultVersion, CompoundTag context, int target, CallbackInfoReturnable<CompoundTag> cir) {
        if (dataFixType == DataFixTypes.CHUNK) LegacyBlockEntities.restore(cir.getReturnValue());
    }
}

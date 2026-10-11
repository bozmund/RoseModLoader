package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.DropCapture;

/** Drops a dying entity spawns go to its LivingDropsEvent first (DropCapture). */
@Mixin(Entity.class)
public abstract class EntityDropCaptureMixin {
    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean rose$captureDrop(ServerLevel level, Entity drop, Operation<Boolean> original) {
        if (drop instanceof ItemEntity item && DropCapture.capture((Entity) (Object) this, item)) return true;
        return original.call(level, drop);
    }
}

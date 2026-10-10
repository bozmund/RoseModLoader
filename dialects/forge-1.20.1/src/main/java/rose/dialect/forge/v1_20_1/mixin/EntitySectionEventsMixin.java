package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/**
 * Forge 1.20.1 EntityJoinLevelEvent on the server: for new entities and for ones loaded with their chunk (both go
 * through the entity manager's addEntity). Canceling keeps the entity out of the level.
 */
@Mixin(PersistentEntitySectionManager.class)
public abstract class EntitySectionEventsMixin {
    @Inject(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"), cancellable = true)
    private void rose$entityJoin(EntityAccess access, boolean loaded, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeDialect.active() || !(access instanceof Entity entity)) return;
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, entity.level()))) cir.setReturnValue(false);
    }
}

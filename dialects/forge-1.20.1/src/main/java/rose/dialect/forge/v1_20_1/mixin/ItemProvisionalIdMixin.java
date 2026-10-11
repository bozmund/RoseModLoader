package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ProvisionalItemIds;

/** Remembers which Properties built an item that got a provisional id, so it can be renamed when registered. */
@Mixin(Item.class)
public abstract class ItemProvisionalIdMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void rose$rememberProperties(Item.Properties properties, CallbackInfo ci) {
        ProvisionalItemIds.constructed((Item) (Object) this, properties);
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.RegistrationKeys;

/** Fills in an item's id from the entry being registered when an old mod didn't set one (see RegistrationKeys). */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
    @Shadow
    private ResourceKey<Item> id;

    @Inject(method = "itemIdOrThrow", at = @At("HEAD"))
    private void rose$defaultId(CallbackInfoReturnable<ResourceKey<Item>> cir) {
        if (id == null) id = RegistrationKeys.current(Registries.ITEM);
    }
}

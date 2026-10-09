package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.dialect.forge.v1_20_1.RegistrationKeys;

/** Fills in a block's id from the entry being registered when an old mod didn't set one (see RegistrationKeys). */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesMixin {
    @Shadow
    private ResourceKey<Block> id;

    @Inject(method = {"effectiveDrops", "effectiveDescriptionId"}, at = @At("HEAD"))
    private void rose$defaultId(CallbackInfoReturnable<?> cir) {
        if (id == null) id = RegistrationKeys.current(Registries.BLOCK);
    }
}

package rose.dialect.forge.v1_20_1.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rose.dialect.forge.v1_20_1.ForgeDialect;

/** Forge 1.20.1 BuildCreativeModeTabContentsEvent, right after a tab's own items (ForgeHooks.onCreativeModeTabBuildContents). */
@Mixin(CreativeModeTab.class)
public abstract class CreativeTabContentsMixin {
    @WrapOperation(method = "buildContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;accept(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V"))
    private void rose$buildContents(CreativeModeTab.DisplayItemsGenerator generator, CreativeModeTab.ItemDisplayParameters parameters,
                                    CreativeModeTab.Output output, Operation<Void> original) {
        original.call(generator, parameters, output);
        if (ForgeDialect.active()) ForgeDialect.buildTabContents(new BuildCreativeModeTabContentsEvent((CreativeModeTab) (Object) this, parameters, output));
    }
}

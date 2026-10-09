package rose.era.v1_20_1.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/** Creates 1.20.1 mods' recipes once item components and tags are bound (see DeferredLegacyRecipe). */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    @Final
    @Mutable
    private RecipeMap recipes;

    @Unique
    private HolderLookup.Provider rose$registries;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rose$remember(HolderLookup.Provider registries, CallbackInfo ci) {
        rose$registries = registries;
    }

    @Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
    private void rose$materialize(FeatureFlagSet enabledFlags, CallbackInfo ci) {
        recipes = LegacyRecipes.materialize(recipes, rose$registries);
    }
}

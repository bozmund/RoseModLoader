package rose.era.v1_20_1.mixin;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.LegacyData;
import rose.era.v1_20_1.crafting.LegacyRecipes;

/**
 * While a registry element file is decoded: 1.20.1 recipe serializers receive the recipe id (26.3 decodes elements
 * without it), and failures in translated mods' files are marked so they're skipped instead of fatal.
 */
@Mixin(targets = "net.minecraft.resources.RegistryLoadTask$PendingRegistration")
public abstract class RecipeIdMixin {
    @Inject(method = "loadFromResource", at = @At("HEAD"))
    private static <T> void rose$setId(Decoder<T> decoder, RegistryOps<JsonElement> ops, ResourceKey<T> key, Resource thunk,
                                       CallbackInfoReturnable<Either<T, Exception>> cir) {
        if (key.registry().equals(Registries.RECIPE.identifier())) LegacyRecipes.setCurrentId(key.identifier());
    }

    @Inject(method = "loadFromResource", at = @At("RETURN"), cancellable = true)
    private static <T> void rose$clearId(Decoder<T> decoder, RegistryOps<JsonElement> ops, ResourceKey<T> key, Resource thunk,
                                         CallbackInfoReturnable<Either<T, Exception>> cir) {
        LegacyRecipes.setCurrentId(null);
        cir.getReturnValue().ifRight(error -> {
            if (LegacyData.isLegacyPack(thunk.sourcePackId())) {
                cir.setReturnValue(Either.right(new LegacyData.Skipped(key.identifier() + " from " + thunk.sourcePackId(), error)));
            }
        });
    }
}

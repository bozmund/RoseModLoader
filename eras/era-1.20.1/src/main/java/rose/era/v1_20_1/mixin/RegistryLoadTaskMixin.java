package rose.era.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.core.WritableRegistry;
import java.util.stream.Stream;
import net.minecraft.resources.RegistryLoadTask;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.LegacyData;
import rose.era.v1_20_1.RegistryLoadContext;
import rose.era.v1_20_1.enchantment.LegacyEnchantments;

/**
 * Errors marked {@link LegacyData.Skipped} (from translated mods' data) don't fail registry loading, nor do
 * references to the skipped elements. Registries loaded from data (not from the network) also get the entries
 * 1.20.1 mods registered in code (enchantments).
 */
@Mixin(RegistryLoadTask.class)
public abstract class RegistryLoadTaskMixin {
    @Shadow
    @Final
    @Mutable
    protected Map<ResourceKey<?>, Exception> loadingErrors;

    @Shadow
    @Final
    private WritableRegistry<?> registry;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rose$tolerateLegacyErrors(CallbackInfo ci) {
        loadingErrors = LegacyData.tolerant(loadingErrors);
    }

    @Inject(method = "freezeRegistry", at = @At("HEAD"))
    private void rose$dropSkippedPlaceholders(Map<ResourceKey<?>, Exception> errors, CallbackInfoReturnable<Boolean> cir) {
        LegacyData.dropSkippedPlaceholders(registry);
    }

    @Inject(method = "registerElements", at = @At("TAIL"))
    private void rose$addLegacyEntries(Stream<?> elements, CallbackInfo ci) {
        if (!((Object) this instanceof RegistryLoadContext loading) || loading.rose$context() == null) return;
        RegistryOps.create(JsonOps.INSTANCE, loading.rose$context()).getter(Registries.ITEM)
                .ifPresent(items -> LegacyEnchantments.addTo(registry, items));
    }
}

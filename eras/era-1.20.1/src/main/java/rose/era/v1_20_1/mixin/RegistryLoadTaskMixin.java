package rose.era.v1_20_1.mixin;

import java.util.Map;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.era.v1_20_1.LegacyData;

/** Errors marked {@link LegacyData.Skipped} (from translated mods' data) don't fail registry loading. */
@Mixin(RegistryLoadTask.class)
public abstract class RegistryLoadTaskMixin {
    @Shadow
    @Final
    @Mutable
    protected Map<ResourceKey<?>, Exception> loadingErrors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rose$tolerateLegacyErrors(CallbackInfo ci) {
        loadingErrors = LegacyData.tolerant(loadingErrors);
    }
}

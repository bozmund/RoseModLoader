package rose.era.v1_20_1.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.era.v1_20_1.RegistryLoadContext;

/** Keeps the lookup a data-driven registry loads with, for entries Rose adds to it (RegistryLoadTaskMixin). */
@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class ResourceManagerRegistryLoadTaskMixin implements RegistryLoadContext {
    @Unique
    private RegistryOps.RegistryInfoLookup rose$context;

    @Inject(method = "load", at = @At("HEAD"))
    private void rose$keepContext(RegistryOps.RegistryInfoLookup context, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        rose$context = context;
    }

    @Override
    public RegistryOps.RegistryInfoLookup rose$context() {
        return rose$context;
    }
}

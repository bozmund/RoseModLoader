package rose.core.mixin;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rose.api.registry.RegistryAliases;

/** Registry lookups of a renamed entry's old id find the entry (see RegistryAliases). */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> {
    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Shadow
    public abstract Optional<Holder.Reference<T>> get(Identifier id);

    @Shadow
    public abstract T getValue(Identifier key);

    @Inject(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void rose$aliasById(Identifier id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isPresent() || id == null) return;
        Identifier to = RegistryAliases.target(key(), id);
        if (to != null) cir.setReturnValue(get(to));
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true)
    private void rose$aliasByKey(ResourceKey<T> id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (cir.getReturnValue().isPresent() || id == null) return;
        Identifier to = RegistryAliases.target(key(), id.identifier());
        if (to != null) cir.setReturnValue(get(to));
    }

    @Inject(method = "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void rose$aliasValueById(Identifier id, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null || id == null) return;
        Identifier to = RegistryAliases.target(key(), id);
        if (to != null) cir.setReturnValue(getValue(to));
    }

    @Inject(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("RETURN"), cancellable = true)
    private void rose$aliasValueByKey(ResourceKey<T> id, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null || id == null) return;
        Identifier to = RegistryAliases.target(key(), id.identifier());
        if (to != null) cir.setReturnValue(getValue(to));
    }
}

package rose.dialect.forge.v1_20_1.mixin.client;

import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rose.dialect.forge.v1_20_1.ForgeDialectClient;

/** Forge's RegisterParticleProvidersEvent, right after vanilla registered its particle providers. */
@Mixin(ParticleResources.class)
public abstract class ParticleResourcesMixin {
    @Inject(method = "registerProviders", at = @At("TAIL"))
    private void rose$modProviders(CallbackInfo ci) {
        ForgeDialectClient.registerParticleProviders((ParticleResources) (Object) this);
    }
}

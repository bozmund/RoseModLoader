package net.minecraftforge.client.event;

import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import rose.era.v1_20_1.client.particle.LegacyParticleProvider;

/** Register particle providers (mod bus, client); fired as vanilla registers its own (ParticleResourcesMixin). */
public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {
    private final ParticleResources resources;

    public RegisterParticleProvidersEvent(ParticleResources resources) {
        this.resources = resources;
    }

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, LegacyParticleProvider<T> provider) {
        resources.register(type, LegacyParticleProvider.adapt(provider));
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, LegacyParticleProvider.Registration<T> registration) {
        resources.register(type, (ParticleResources.SpriteParticleRegistration<T>) sprites -> LegacyParticleProvider.adapt(registration.create(sprites)));
    }
}

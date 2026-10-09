package net.minecraftforge.client.event;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/** Register particle providers (mod bus, client). */
public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {
    private final ParticleResources resources;

    public RegisterParticleProvidersEvent(ParticleResources resources) {
        this.resources = resources;
    }

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
        resources.register(type, provider);
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleResources.SpriteParticleRegistration<T> registration) {
        resources.register(type, registration);
    }
}

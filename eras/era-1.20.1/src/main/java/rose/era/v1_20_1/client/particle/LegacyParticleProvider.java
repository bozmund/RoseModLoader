package rose.era.v1_20_1.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

/**
 * 1.20.1 {@code ParticleProvider}: created a particle without the RandomSource 26.x passes. Mod providers are
 * rebased onto this; {@link #adapt} makes one a 26.3 provider.
 */
public interface LegacyParticleProvider<T extends ParticleOptions> {
    Particle createParticle(T options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd);

    /** 1.20.1 {@code ParticleEngine.SpriteParticleRegistration}: a provider made from the particle's sprites. */
    interface Registration<T extends ParticleOptions> {
        LegacyParticleProvider<T> create(SpriteSet sprites);
    }

    static <T extends ParticleOptions> ParticleProvider<T> adapt(LegacyParticleProvider<T> legacy) {
        return (options, level, x, y, z, xd, yd, zd, random) -> legacy.createParticle(options, level, x, y, z, xd, yd, zd);
    }
}

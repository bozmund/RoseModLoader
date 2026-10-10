package rose.era.v1_20_1.client.particle;

import net.minecraft.client.particle.SingleQuadParticle;

/**
 * 1.20.1 {@code ParticleRenderType}: the drawing pass of a particle. 26.x draws textured particles in a
 * SingleQuadParticle.Layer; the old passes map onto the layers (LIT was opaque with a full-bright light, which the
 * particle's light coords already give). A mod's own pass is drawn translucent.
 */
public interface LegacyParticleRenderType {
    LegacyParticleRenderType TERRAIN_SHEET = new Pass(SingleQuadParticle.Layer.OPAQUE_TERRAIN);
    LegacyParticleRenderType PARTICLE_SHEET_OPAQUE = new Pass(SingleQuadParticle.Layer.OPAQUE);
    LegacyParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new Pass(SingleQuadParticle.Layer.TRANSLUCENT);
    LegacyParticleRenderType PARTICLE_SHEET_LIT = new Pass(SingleQuadParticle.Layer.OPAQUE);
    LegacyParticleRenderType CUSTOM = new Pass(SingleQuadParticle.Layer.TRANSLUCENT);
    LegacyParticleRenderType NO_RENDER = new Pass(SingleQuadParticle.Layer.TRANSLUCENT);

    default SingleQuadParticle.Layer layer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    /** One of 1.20.1's own passes. */
    record Pass(SingleQuadParticle.Layer layer) implements LegacyParticleRenderType {}
}

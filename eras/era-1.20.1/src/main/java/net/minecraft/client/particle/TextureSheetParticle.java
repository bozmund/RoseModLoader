package net.minecraft.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import rose.era.v1_20_1.client.particle.LegacyParticleRenderType;

/**
 * Era bridge (1.20.1): a particle drawn as one textured quad. 26.x calls it SingleQuadParticle, takes the sprite in
 * the constructor and picks its pass with a layer; old particles set their sprite after construction (pickSprite,
 * setSpriteFromAge) and name a render type.
 */
public abstract class TextureSheetParticle extends SingleQuadParticle {
    protected TextureSheetParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, null);
    }

    protected TextureSheetParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, null);
    }

    public abstract LegacyParticleRenderType getRenderType();

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        LegacyParticleRenderType type = getRenderType();
        return type == null ? SingleQuadParticle.Layer.TRANSLUCENT : type.layer();
    }

    public void pickSprite(SpriteSet sprites) {
        setSprite(sprites.get(random));
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
    }

    /** 1.20.1's name for the light coords; mod particles override this one. */
    protected int getLightColor(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return getLightColor(partialTick);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        if (sprite != null) super.extract(state, camera, partialTick);
    }
}

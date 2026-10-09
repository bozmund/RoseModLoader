package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import rose.era.v1_20_1.client.render.LegacyBlockEntityRenderer;

/**
 * Era bridge (1.20.1): the sign renderer mods extended for custom signs. 26.x split it into render-state renderers
 * (StandingSignRenderer, AbstractSignRenderer). This keeps subclasses loadable; their immediate-mode drawing
 * (models into vertex consumers) is not bridged yet, so they draw nothing (see LegacyBlockEntityRendererAdapter).
 */
public class SignRenderer implements LegacyBlockEntityRenderer<SignBlockEntity> {
    public SignRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void submit(SignBlockEntity sign, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
    }

    /** 1.20.1 sign model: a board and a stick. */
    public static class SignModel extends Model<Object> {
        public final ModelPart stick;

        public SignModel(ModelPart root) {
            super(root, texture -> null);
            this.stick = root.getChild("stick");
        }
    }
}

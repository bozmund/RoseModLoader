package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Era bridge (1.20.1): where a renderer drew its vertices, per render type. 26.3 renderers submit nodes to a
 * SubmitNodeCollector instead; Rose hands old renderers a {@link rose.era.v1_20_1.client.render.LegacyBuffers}.
 */
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}

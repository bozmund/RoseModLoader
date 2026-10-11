package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * The buffers an old renderer gets: drawing items and blocks goes to 26.3's collector (see ItemRenderer). Raw
 * vertices ({@link #getBuffer}) are recorded per render type and submitted as custom geometry, which 26.3 draws later
 * in the frame, when the renderer has finished writing them. The renderer already applied its pose stack to them, so
 * they are submitted with an identity pose.
 */
public final class LegacyBuffers implements MultiBufferSource {
    private final SubmitNodeCollector collector;
    private final Map<RenderType, RecordedVertices> buffers = new HashMap<>();

    public LegacyBuffers(SubmitNodeCollector collector, String renderer) {
        this.collector = collector;
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, type -> {
            RecordedVertices recorded = new RecordedVertices();
            collector.submitCustomGeometry(new PoseStack(), type, (pose, buffer) -> recorded.replay(buffer));
            return recorded;
        });
    }
}

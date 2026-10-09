package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * The buffers an old renderer gets: drawing items and blocks goes to 26.3's collector (see ItemRenderer). Raw
 * vertices ({@link #getBuffer}) aren't bridged yet; they are dropped and reported once per renderer.
 */
public record LegacyBuffers(SubmitNodeCollector collector, String renderer) implements MultiBufferSource {
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (REPORTED.add(renderer)) {
            LogUtils.getLogger().warn("[rose] {} draws raw vertices; 26.3 renders through submitted nodes, so they are not drawn yet", renderer);
        }
        return NoVertices.INSTANCE;
    }
}

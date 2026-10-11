package rose.era.v1_20_1.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * 1.20.1 {@code RenderType} block layers. 26.x keeps chunk layers apart (ChunkSectionLayer); block-shaped drawing
 * outside chunks (block entity renderers, moving blocks) uses RenderTypes' moving-block types, the closest match.
 */
public final class RenderTypeShim {
    private RenderTypeShim() {}

    public static RenderType solid() {
        return RenderTypes.solidMovingBlock();
    }

    public static RenderType cutout() {
        return RenderTypes.cutoutMovingBlock();
    }

    public static RenderType translucent() {
        return RenderTypes.translucentMovingBlock();
    }
}

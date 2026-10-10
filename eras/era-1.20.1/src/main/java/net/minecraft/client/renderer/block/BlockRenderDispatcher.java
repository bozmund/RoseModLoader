package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import rose.era.v1_20_1.client.render.LegacyBuffers;

/**
 * Era bridge (1.20.1): {@code Minecraft.getBlockRenderer()}. 26.3 resolves a block state into a BlockModelRenderState
 * and submits it; {@link #renderSingleBlock} does that with the collector behind the old renderer's buffers.
 */
public class BlockRenderDispatcher {
    public static final BlockRenderDispatcher INSTANCE = new BlockRenderDispatcher();
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    public void renderSingleBlock(BlockState state, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (state.getRenderShape() != RenderShape.MODEL || !(buffers instanceof LegacyBuffers legacy)) return;
        BlockModelRenderState model = new BlockModelRenderState();
        new BlockModelResolver(Minecraft.getInstance().getModelManager()).update(model, state, DISPLAY_CONTEXT);
        model.submit(pose, legacy.collector(), light, overlay, 0);
    }
}

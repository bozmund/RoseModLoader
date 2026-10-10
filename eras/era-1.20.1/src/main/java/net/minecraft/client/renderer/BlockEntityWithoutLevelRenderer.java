package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Era bridge (1.20.1): the renderer behind {@code builtin/entity} item models. 1.21.4 replaced it with special model
 * renderers; mods subclass this and override {@link #renderByItem}, which rose:legacy_renderer item models call (see
 * LegacyItemRenderer). Vanilla's own built-in items render through 26.3's special models, so the base draws nothing.
 */
public class BlockEntityWithoutLevelRenderer implements ResourceManagerReloadListener {
    public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
    }
}

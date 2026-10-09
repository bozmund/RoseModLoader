package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rose.era.v1_20_1.client.render.LegacyBakedModel;
import rose.era.v1_20_1.client.render.LegacyBuffers;

/**
 * Era bridge (1.20.1): drew item stacks immediately. 26.3 resolves an item into an ItemStackRenderState and submits
 * it; {@link #renderStatic} does that with the collector behind the old renderer's buffers.
 */
public class ItemRenderer {
    public static final ItemRenderer INSTANCE = new ItemRenderer();

    public void renderStatic(ItemStack stack, ItemDisplayContext context, int light, int overlay, PoseStack pose,
                             MultiBufferSource buffers, Level level, int seed) {
        renderStatic(null, stack, context, false, pose, buffers, level, light, overlay, seed);
    }

    public void renderStatic(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack pose,
                             MultiBufferSource buffers, Level level, int light, int overlay, int seed) {
        if (stack.isEmpty() || !(buffers instanceof LegacyBuffers legacy)) return;
        ItemStackRenderState state = resolve(stack, context, level, entity, seed);
        state.submit(pose, legacy.collector(), light, overlay, 0);
    }

    /** 1.20.1 {@code getModel(stack, level, entity, seed)}: only its properties are bridged (see LegacyBakedModel). */
    public LegacyBakedModel getModel(ItemStack stack, Level level, LivingEntity entity, int seed) {
        ItemStackRenderState state = resolve(stack, ItemDisplayContext.FIXED, level, entity, seed);
        return new LegacyBakedModel.Resolved(state.usesBlockLight());
    }

    private static ItemStackRenderState resolve(ItemStack stack, ItemDisplayContext context, Level level, LivingEntity entity, int seed) {
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, level, entity, seed);
        return state;
    }
}

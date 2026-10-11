package rose.era.v1_20_1.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;

/** Redirect targets for 1.20.1 rendering entry points. */
public final class RenderShim {
    /** 1.20.1 {@code Minecraft.getItemRenderer()}; 26.3 renders items through ItemModelResolver. */
    public static ItemRenderer itemRenderer(Minecraft minecraft) {
        return ItemRenderer.INSTANCE;
    }

    /** 1.20.1 {@code BlockEntityRendererProvider.Context.getItemRenderer()}. */
    public static ItemRenderer itemRenderer(BlockEntityRendererProvider.Context context) {
        return ItemRenderer.INSTANCE;
    }

    /** 1.20.1 {@code Minecraft.getBlockRenderer()}; 26.3 resolves block models through BlockModelResolver. */
    public static net.minecraft.client.renderer.block.BlockRenderDispatcher blockRenderer(Minecraft minecraft) {
        return net.minecraft.client.renderer.block.BlockRenderDispatcher.INSTANCE;
    }

    /** 1.20.1 {@code Minecraft.getFrameTime()}: the partial tick, which 26.3's DeltaTracker keeps. */
    public static float getFrameTime(Minecraft minecraft) {
        return minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    /** 1.20.1 {@code LevelRenderer.getLightColor(level, pos)}: 26.3 {@code LightCoordsUtil.getLightCoords}. */
    public static int getLightColor(BlockAndTintGetter level, BlockPos pos) {
        return level instanceof BlockAndLightGetter light ? LightCoordsUtil.getLightCoords(light, pos) : LightCoordsUtil.FULL_BRIGHT;
    }

    /** 1.20.1 {@code PoseStack.mulPose(quaternion)}; 26.x calls it {@code rotate} ({@code mulPose} takes matrices). */
    public static void mulPose(com.mojang.blaze3d.vertex.PoseStack pose, org.joml.Quaternionf rotation) {
        pose.rotate(rotation);
    }

    private RenderShim() {}

    /** 1.20.1 {@code BlockEntityRenderers.register(type, provider)} with an old-style provider, run through the adapter. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerBlockEntityRenderer(
            net.minecraft.world.level.block.entity.BlockEntityType<? extends T> type, LegacyBlockEntityRendererProvider<T> provider) {
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register((net.minecraft.world.level.block.entity.BlockEntityType) type,
                (net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider) LegacyBlockEntityRendererAdapter.provider(type, provider));
    }

    /** 1.20.1 {@code BlockEntityRendererProvider.Context.getBlockEntityRenderDispatcher()}; 26.3's Context has none. */
    public static net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher blockEntityRenderDispatcher(BlockEntityRendererProvider.Context context) {
        return Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    /** 1.20.1 {@code BlockEntityRenderDispatcher.camera}: 26.3 keeps only the camera position there; the camera is the game renderer's. */
    public static net.minecraft.client.Camera camera(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher dispatcher) {
        return Minecraft.getInstance().gameRenderer.mainCamera();
    }

    /** 1.20.1 {@code Camera.getPosition()}; 26.3 calls it {@code position()}. */
    public static net.minecraft.world.phys.Vec3 cameraPosition(net.minecraft.client.Camera camera) {
        return camera.position();
    }

    /**
     * 1.20.1 {@code BlockEntityRenderDispatcher.renderItem}: draws a block entity outside the world (item renderers).
     * An old renderer draws straight into the buffers; a 26.3 renderer would need the submit pipeline, so it is skipped.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean renderItem(net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher dispatcher,
                                     net.minecraft.world.level.block.entity.BlockEntity blockEntity, com.mojang.blaze3d.vertex.PoseStack pose,
                                     net.minecraft.client.renderer.MultiBufferSource buffers, int light, int overlay) {
        if (!(dispatcher.getRenderer(blockEntity) instanceof LegacyBlockEntityRendererAdapter adapter)) return false;
        adapter.legacy().submit(blockEntity, 0.0F, pose, buffers, light, overlay);
        return true;
    }

    /** 1.20.1 {@code Material.sprite()} (26.3 {@code SpriteId}): the sprite from its atlas. */
    public static net.minecraft.client.renderer.texture.TextureAtlasSprite sprite(net.minecraft.client.resources.model.sprite.SpriteId material) {
        return Minecraft.getInstance().getAtlasManager().get(material);
    }

    /** 1.20.1 {@code Material.buffer(buffers, renderType)}: the atlas's render type, with UVs mapped into the sprite. */
    public static com.mojang.blaze3d.vertex.VertexConsumer materialBuffer(net.minecraft.client.resources.model.sprite.SpriteId material,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            java.util.function.Function<net.minecraft.resources.Identifier, net.minecraft.client.renderer.rendertype.RenderType> renderType) {
        return sprite(material).wrap(buffers.getBuffer(material.renderType(renderType)));
    }

    /** As above with 1.20.1's foil flag; enchantment glint isn't drawn for these. */
    public static com.mojang.blaze3d.vertex.VertexConsumer materialBuffer(net.minecraft.client.resources.model.sprite.SpriteId material,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            java.util.function.Function<net.minecraft.resources.Identifier, net.minecraft.client.renderer.rendertype.RenderType> renderType, boolean foil) {
        return materialBuffer(material, buffers, renderType);
    }
}

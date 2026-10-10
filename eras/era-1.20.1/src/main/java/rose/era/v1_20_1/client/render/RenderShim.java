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
}

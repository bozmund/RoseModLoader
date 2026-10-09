package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 1.20.1 {@code BlockEntityRenderer<T>}: drew a block entity immediately. Its {@code render} method carries 26.3's
 * name for it ({@code submit}, via intermediary); {@link LegacyBlockEntityRendererAdapter} calls it at submit time.
 */
public interface LegacyBlockEntityRenderer<T extends BlockEntity> {
    void submit(T blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay);

    default boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(T blockEntity, Vec3 cameraPosition) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPosition, getViewDistance());
    }
}

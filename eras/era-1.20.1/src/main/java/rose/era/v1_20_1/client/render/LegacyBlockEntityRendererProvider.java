package rose.era.v1_20_1.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 1.20.1 {@code BlockEntityRendererProvider<T>}: creates an old renderer (26.3's Context is the same). */
@FunctionalInterface
public interface LegacyBlockEntityRendererProvider<T extends BlockEntity> {
    LegacyBlockEntityRenderer<T> create(BlockEntityRendererProvider.Context context);
}

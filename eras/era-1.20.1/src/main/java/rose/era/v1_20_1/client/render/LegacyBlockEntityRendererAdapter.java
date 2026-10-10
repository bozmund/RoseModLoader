package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Runs a 1.20.1 block entity renderer inside 26.3's two-phase rendering: extracting remembers the block entity,
 * submitting calls the old {@code render} with buffers that forward to the collector.
 */
public final class LegacyBlockEntityRendererAdapter<T extends BlockEntity>
        implements BlockEntityRenderer<T, LegacyBlockEntityRendererAdapter.State> {
    private final LegacyBlockEntityRenderer<T> legacy;
    private boolean failed;

    public LegacyBlockEntityRendererAdapter(LegacyBlockEntityRenderer<T> legacy) {
        this.legacy = legacy;
    }

    /** A dialect's provider wrapper: 26.3 creates renderers from a Context, as 1.20.1 did. */
    public static <T extends BlockEntity> BlockEntityRendererProvider<T, State> provider(LegacyBlockEntityRendererProvider<T> legacy) {
        return provider(null, legacy);
    }

    /**
     * As {@link #provider(LegacyBlockEntityRendererProvider)}, for {@code type}: when the old renderer can't be built,
     * the block entity gets the vanilla renderer of the vanilla block entity it extends (a mod sign is still a sign),
     * else nothing is drawn.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntity> BlockEntityRendererProvider<T, State> provider(@Nullable BlockEntityType<?> type,
                                                                                         LegacyBlockEntityRendererProvider<T> legacy) {
        return context -> {
            try {
                return new LegacyBlockEntityRendererAdapter<>(legacy.create(context));
            } catch (RuntimeException | LinkageError e) {
                // 26.3 fails the whole resource reload if one renderer can't be built; an old renderer that uses an
                // API Rose doesn't bridge yet falls back (or draws nothing) instead.
                if (Boolean.getBoolean("rose.forge.strict")) throw e;
                BlockEntityRendererProvider<?, ?> vanilla = type == null ? null : VanillaFallback.providerFor(type);
                LogUtils.getLogger().warn("[rose] block entity renderer from {} not created ({}): {}", legacy.getClass().getName(),
                        vanilla != null ? "using the vanilla renderer of its block entity's superclass" : "it draws nothing", e.toString());
                if (vanilla != null) return (BlockEntityRenderer) vanilla.create(context);
                return new LegacyBlockEntityRendererAdapter<>((be, partialTick, pose, buffers, light, overlay) -> { });
            }
        };
    }

    public static final class State extends BlockEntityRenderState {
        BlockEntity blockEntity;
        float partialTick;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T blockEntity, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTick = partialTicks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.blockEntity == null || failed) return;
        pose.pushPose();
        try {
            legacy.submit((T) state.blockEntity, state.partialTick, pose, new LegacyBuffers(collector, legacy.getClass().getName()),
                    state.lightCoords, OverlayTexture.NO_OVERLAY);
        } catch (RuntimeException | LinkageError e) {
            // An old renderer using an API Rose doesn't bridge yet shouldn't crash the game: it stops drawing.
            if (Boolean.getBoolean("rose.forge.strict")) throw e;
            failed = true;
            LogUtils.getLogger().warn("[rose] block entity renderer {} disabled: {}", legacy.getClass().getName(), e.toString());
        } finally {
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPosition) {
        return legacy.shouldRender(blockEntity, cameraPosition);
    }

    @Override
    public int getViewDistance() {
        return legacy.getViewDistance();
    }
}

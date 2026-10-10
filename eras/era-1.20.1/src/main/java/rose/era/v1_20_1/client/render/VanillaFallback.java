package rose.era.v1_20_1.client.render;

import java.util.Map;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Finds the vanilla renderer for a mod block entity type: the one of the vanilla block entity class it extends. */
final class VanillaFallback {
    static @Nullable BlockEntityRendererProvider<?, ?> providerFor(BlockEntityType<?> type) {
        BlockEntity sample = sample(type);
        if (sample == null) return null;
        Class<?> vanilla = sample.getClass();
        while (vanilla != null && !vanilla.getName().startsWith("net.minecraft.")) vanilla = vanilla.getSuperclass();
        if (vanilla == null || vanilla == BlockEntity.class) return null;
        Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> providers = BlockEntityRenderers.PROVIDERS;
        for (var entry : providers.entrySet()) {
            var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entry.getKey());
            if (key == null || !key.getNamespace().equals("minecraft")) continue;
            BlockEntity vanillaSample = sample(entry.getKey());
            if (vanillaSample != null && vanillaSample.getClass() == vanilla) return entry.getValue();
        }
        return null;
    }

    /** A block entity of this type, made for any block it accepts; {@code null} if none can be made. */
    private static @Nullable BlockEntity sample(BlockEntityType<?> type) {
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            if (!type.isValid(state)) continue;
            try {
                return type.create(BlockPos.ZERO, state);
            } catch (RuntimeException | LinkageError e) {
                return null;
            }
        }
        return null;
    }

    private VanillaFallback() {}
}

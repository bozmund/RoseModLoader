package rose.era.v1_20_1.client.render;

import java.util.Map;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;
import rose.era.v1_20_1.block.LegacyBlockEntities;

/** Finds the vanilla renderer for a mod block entity type: the one of the vanilla block entity class it extends. */
final class VanillaFallback {
    static @Nullable BlockEntityRendererProvider<?, ?> providerFor(BlockEntityType<?> type) {
        Class<?> vanilla = LegacyBlockEntities.vanillaClass(type);
        if (vanilla == null) return null;
        Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> providers = BlockEntityRenderers.PROVIDERS;
        for (var entry : providers.entrySet()) {
            var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entry.getKey());
            if (key == null || !key.getNamespace().equals("minecraft")) continue;
            BlockEntity vanillaSample = LegacyBlockEntities.sample(entry.getKey());
            if (vanillaSample != null && vanillaSample.getClass() == vanilla) return entry.getValue();
        }
        return null;
    }

    private VanillaFallback() {}
}

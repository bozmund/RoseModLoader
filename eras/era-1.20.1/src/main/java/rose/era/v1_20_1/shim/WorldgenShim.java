package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Redirect targets for 1.20.1 configured features. 26.x folded the configuration into the feature: 1.20.1's
 * ConfiguredFeature is the 26.3 Feature interface (class-renamed), kept in {@code worldgen/feature}.
 */
public final class WorldgenShim {
    /** 1.20.1 {@code Registries.CONFIGURED_FEATURE} ({@code worldgen/configured_feature}): 26.3 {@code Registries.FEATURE}. */
    public static ResourceKey<Registry<Feature>> CONFIGURED_FEATURE() {
        return Registries.FEATURE;
    }

    /** 1.20.1 {@code ConfiguredFeature.place(level, generator, random, origin)}; same signature on the 26.3 interface. */
    public static boolean place(Feature self, WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return self.place(level, generator, random, origin);
    }

    private WorldgenShim() {}
}

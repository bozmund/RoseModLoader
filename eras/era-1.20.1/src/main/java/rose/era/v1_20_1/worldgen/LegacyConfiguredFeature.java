package rose.era.v1_20_1.worldgen;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** A 1.20.1 feature with its configuration: a 26.3 {@code Feature}. */
public record LegacyConfiguredFeature<FC extends FeatureConfiguration>(LegacyFeature<FC> feature, FC config) implements Feature {
    @Override
    public MapCodec<? extends Feature> codec() {
        return feature.typeCodec();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return feature.place(new FeaturePlaceContext<>(Optional.of(this), level, chunkGenerator, random, origin, config));
    }
}

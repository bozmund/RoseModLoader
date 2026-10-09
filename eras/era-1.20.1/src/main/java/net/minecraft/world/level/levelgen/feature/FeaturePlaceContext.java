package net.minecraft.world.level.levelgen.feature;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Era bridge (1.20.1): what a feature gets when placed. 26.3 passes level, generator, random and origin directly. */
public class FeaturePlaceContext<FC extends FeatureConfiguration> {
    private final Optional<Feature> topFeature;
    private final WorldGenLevel level;
    private final ChunkGenerator chunkGenerator;
    private final RandomSource random;
    private final BlockPos origin;
    private final FC config;

    public FeaturePlaceContext(Optional<Feature> topFeature, WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random,
                               BlockPos origin, FC config) {
        this.topFeature = topFeature;
        this.level = level;
        this.chunkGenerator = chunkGenerator;
        this.random = random;
        this.origin = origin;
        this.config = config;
    }

    public Optional<Feature> topFeature() {
        return topFeature;
    }

    public WorldGenLevel level() {
        return level;
    }

    public ChunkGenerator chunkGenerator() {
        return chunkGenerator;
    }

    public RandomSource random() {
        return random;
    }

    public BlockPos origin() {
        return origin;
    }

    public FC config() {
        return config;
    }
}

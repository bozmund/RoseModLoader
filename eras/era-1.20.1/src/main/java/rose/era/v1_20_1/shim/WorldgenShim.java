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

    private static final java.util.Map<net.minecraft.world.level.levelgen.RandomState, net.minecraft.world.level.biome.Climate.Sampler> SAMPLERS =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final java.util.Map<net.minecraft.world.level.biome.Climate.Sampler, java.util.Map<net.minecraft.world.level.biome.BiomeSource,
            net.minecraft.world.level.biome.BiomeResolver>> RESOLVERS = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /** 1.20.1 {@code RandomState.sampler()}: the climate sampler; 26.x builds samplers for a context (here uncached). */
    public static net.minecraft.world.level.biome.Climate.Sampler sampler(net.minecraft.world.level.levelgen.RandomState self) {
        return SAMPLERS.computeIfAbsent(self, s -> s.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED));
    }

    /**
     * 1.20.1 {@code BiomeSource.getNoiseBiome(quartX, quartY, quartZ, sampler)}; 26.x asks a BiomeResolver the source
     * makes for a sampler.
     */
    public static net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getNoiseBiome(net.minecraft.world.level.biome.BiomeSource self,
            int quartX, int quartY, int quartZ, net.minecraft.world.level.biome.Climate.Sampler sampler) {
        var bySource = RESOLVERS.computeIfAbsent(sampler, s -> java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>()));
        return bySource.computeIfAbsent(self, source -> source.createResolver(sampler)).getNoiseBiome(quartX, quartY, quartZ);
    }
}

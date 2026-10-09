package net.minecraftforge.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Forge's built-in biome modifiers. */
public final class ForgeBiomeModifiers {
    public record AddFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step)
            implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biomes.contains(biome)) {
                features.forEach(feature -> builder.getGenerationSettings().addFeature(step, feature));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            throw new UnsupportedOperationException("Biome modifier codecs are not wired up on Rose yet");
        }
    }

    private ForgeBiomeModifiers() {}
}

package net.minecraftforge.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Forge's built-in biome modifiers. */
public final class ForgeBiomeModifiers {
    public record AddFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step)
            implements BiomeModifier {
        public static final Codec<AddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
                RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
                RegistryCodecs.holderSet(Registries.PLACED_FEATURE).fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
                GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step)
        ).apply(i, AddFeaturesBiomeModifier::new));

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biomes.contains(biome)) {
                features.forEach(feature -> builder.getGenerationSettings().addFeature(step, feature));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }

    private ForgeBiomeModifiers() {}
}

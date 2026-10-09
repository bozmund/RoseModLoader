package net.minecraftforge.common.world;

/** What a biome modifier may change about a biome. */
public class ModifiableBiomeInfo {
    public record BiomeInfo() {
        public static class Builder {
            private final BiomeGenerationSettingsBuilder generationSettings = new BiomeGenerationSettingsBuilder();

            public BiomeGenerationSettingsBuilder getGenerationSettings() {
                return generationSettings;
            }
        }
    }
}

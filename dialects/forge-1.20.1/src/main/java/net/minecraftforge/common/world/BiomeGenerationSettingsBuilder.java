package net.minecraftforge.common.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** A biome generation settings builder that biome modifiers add features to; Rose merges the additions in. */
public class BiomeGenerationSettingsBuilder extends BiomeGenerationSettings.PlainBuilder {
    private final List<List<Holder<PlacedFeature>>> added = new ArrayList<>();

    @Override
    public BiomeGenerationSettings.PlainBuilder addFeature(int index, Holder<PlacedFeature> feature) {
        while (added.size() <= index) added.add(new ArrayList<>());
        added.get(index).add(feature);
        return super.addFeature(index, feature);
    }

    /** Features added per step index, in order (Rose merges them into the biome). */
    public List<List<Holder<PlacedFeature>>> rose$added() {
        return added;
    }

    public boolean rose$hasAdditions() {
        return added.stream().anyMatch(step -> !step.isEmpty());
    }
}

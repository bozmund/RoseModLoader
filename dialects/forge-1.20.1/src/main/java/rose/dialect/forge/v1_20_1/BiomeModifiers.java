package rose.dialect.forge.v1_20_1;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Forge biome modifiers ({@code data/<ns>/forge/biome_modifier/*.json}), applied when the server is about to start
 * as Forge does: each biome's features get the modifiers' additions, then the chunk generators re-sort their
 * per-step feature lists (26.3 may have computed them while validating the world).
 */
public final class BiomeModifiers {
    private static final Logger LOG = LogUtils.getLogger();
    private static final FileToIdConverter FILES = FileToIdConverter.json("forge/biome_modifier");

    /** Forge's own modifier types, in the serializer registry mods also register into. */
    public static void registerForgeTypes() {
        Registry.register(ForgeRegistryLookup.get(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS).vanilla(),
                Identifier.fromNamespaceAndPath("forge", "add_features"), ForgeBiomeModifiers.AddFeaturesBiomeModifier.CODEC);
    }

    public static void apply(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        List<BiomeModifier> modifiers = load(server.getResourceManager(), access);
        if (modifiers.isEmpty()) return;
        Registry<Biome> biomes = access.lookupOrThrow(Registries.BIOME);
        int changed = 0;
        for (Holder.Reference<Biome> biome : biomes.listElements().toList()) {
            var info = new ModifiableBiomeInfo.BiomeInfo.Builder();
            for (BiomeModifier.Phase phase : BiomeModifier.Phase.values()) {
                for (BiomeModifier modifier : modifiers) {
                    try {
                        modifier.modify(biome, phase, info);
                    } catch (RuntimeException | LinkageError e) {
                        if (Unsupported.STRICT) throw e;
                        LOG.warn("[rose/forge] biome modifier {} failed on {}", modifier, biome.key().identifier(), e);
                    }
                }
            }
            if (!info.getGenerationSettings().rose$hasAdditions()) continue;
            biome.value().generationSettings = merged(biome.value().getGenerationSettings(), info.getGenerationSettings().rose$added());
            changed++;
        }
        for (LevelStem stem : access.lookupOrThrow(Registries.LEVEL_STEM)) refresh(stem.generator());
        LOG.info("[rose/forge] {} biome modifier(s) changed {} biome(s)", modifiers.size(), changed);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<BiomeModifier> load(ResourceManager resources, RegistryAccess access) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);
        Registry<Codec<?>> serializers = ForgeRegistryLookup.<Codec<?>>get(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS).vanilla();
        List<BiomeModifier> out = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> file : FILES.listMatchingResources(resources).entrySet()) {
            Identifier id = FILES.fileToId(file.getKey());
            try (Reader reader = file.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                Identifier type = Identifier.parse(json.getAsJsonObject().get("type").getAsString());
                Codec<?> codec = (Codec<?>) RegistryValues.get((Registry) serializers, type);
                if (codec == null) {
                    LOG.warn("[rose/forge] biome modifier {} has unknown type {}", id, type);
                    continue;
                }
                out.add((BiomeModifier) codec.parse(ops, json).getOrThrow());
            } catch (Exception e) {
                if (Unsupported.STRICT) throw new IllegalStateException("Biome modifier " + id, e);
                LOG.warn("[rose/forge] biome modifier {} skipped: {}", id, e.toString());
            }
        }
        return out;
    }

    private static BiomeGenerationSettings merged(BiomeGenerationSettings old, List<List<Holder<PlacedFeature>>> added) {
        BiomeGenerationSettings.PlainBuilder builder = new BiomeGenerationSettings.PlainBuilder();
        old.getCarvers().forEach(builder::addCarver);
        List<net.minecraft.core.HolderSet<PlacedFeature>> steps = old.features();
        for (int i = 0; i < Math.max(steps.size(), added.size()); i++) {
            if (i < steps.size()) for (Holder<PlacedFeature> f : steps.get(i)) builder.addFeature(i, f);
            if (i < added.size()) for (Holder<PlacedFeature> f : added.get(i)) builder.addFeature(i, f);
        }
        return builder.build();
    }

    /** ChunkGenerator memoizes its per-step feature order; rebuild it from the (changed) biomes. */
    private static void refresh(ChunkGenerator generator) {
        generator.featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(
                List.copyOf(generator.getBiomeSource().possibleBiomes()), b -> generator.getBiomeGenerationSettings(b).features(), true));
    }

    private BiomeModifiers() {}
}

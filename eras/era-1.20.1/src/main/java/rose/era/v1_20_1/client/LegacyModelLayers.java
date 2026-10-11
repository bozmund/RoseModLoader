package rose.era.v1_20_1.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/**
 * Model layers old mods define (Forge's RegisterLayerDefinitions). 26.3 bakes only {@code LayerDefinitions.createRoots()};
 * EntityModelSetMixin falls back to these, so {@code EntityModelSet.bakeLayer} finds them.
 */
public final class LegacyModelLayers {
    private static final Map<ModelLayerLocation, Supplier<LayerDefinition>> LAYERS = new ConcurrentHashMap<>();

    public static void register(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        LAYERS.put(location, definition);
    }

    public static Map<ModelLayerLocation, Supplier<LayerDefinition>> all() {
        return LAYERS;
    }

    private LegacyModelLayers() {}
}

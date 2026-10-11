package rose.era.v1_20_1.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;

/**
 * 1.20.1 {@code SimpleJsonResourceReloadListener(gson, directory)}: the JSON files under {@code directory}, by id, as
 * raw JSON. 1.21.2 made the class generic over a codec; raw JSON is its {@code ExtraCodecs.JSON} case. The mod's
 * {@code apply(Map<Identifier, JsonElement>, ...)} overrides the same method.
 */
public abstract class LegacySimpleJsonResourceReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    protected LegacySimpleJsonResourceReloadListener(Gson gson, String directory) {
        super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
    }
}

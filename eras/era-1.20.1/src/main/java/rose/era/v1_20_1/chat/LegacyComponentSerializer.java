package rose.era.v1_20_1.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import rose.era.v1_20_1.EraContext;

/**
 * 1.20.1 {@code Component.Serializer}: text components to and from JSON strings. 1.20.3 replaced the Gson adapter with
 * {@link ComponentSerialization#CODEC}; this goes through that codec with the game's registries.
 */
public class LegacyComponentSerializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
    @Override
    public MutableComponent deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return fromJson(json);
    }

    @Override
    public JsonElement serialize(Component component, Type type, JsonSerializationContext context) {
        return toJsonTree(component);
    }

    public static String toJson(Component component) {
        return toJsonTree(component).toString();
    }

    public static String toStableJson(Component component) {
        return toJson(component);
    }

    public static JsonElement toJsonTree(Component component) {
        return ComponentSerialization.CODEC.encodeStart(ops(), component).getOrThrow(JsonParseException::new);
    }

    /** {@code null} for {@code null} input, like 1.20.1. */
    public static MutableComponent fromJson(String json) {
        return json == null ? null : fromJson(JsonParser.parseString(json));
    }

    public static MutableComponent fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        return ComponentSerialization.CODEC.parse(ops(), json).getOrThrow(JsonParseException::new).copy();
    }

    public static MutableComponent fromJsonLenient(String json) {
        if (json == null) return null;
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setStrictness(Strictness.LENIENT);
        return fromJson(JsonParser.parseReader(reader));
    }

    private static RegistryOps<JsonElement> ops() {
        return EraContext.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }
}

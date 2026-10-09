package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/** Era bridge (1.20.1): JSON (de)serializer for loot functions and conditions. 26.3 uses MapCodecs. */
public interface Serializer<T> {
    void serialize(JsonObject json, T value, JsonSerializationContext context);

    T deserialize(JsonObject json, JsonDeserializationContext context);
}

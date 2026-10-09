package net.minecraftforge.common.crafting.conditions;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

/** Reads and writes one condition type ({@code "type": "<id>"} in JSON). */
public interface IConditionSerializer<T extends ICondition> {
    void write(JsonObject json, T value);

    T read(JsonObject json);

    Identifier getID();

    default JsonObject getJson(T value) {
        JsonObject json = new JsonObject();
        write(json, value);
        json.addProperty("type", value.getID().toString());
        return json;
    }
}

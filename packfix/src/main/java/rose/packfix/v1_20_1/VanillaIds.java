package rose.packfix.v1_20_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;

/**
 * Vanilla item and block ids renamed after 1.20.1 (data version 3465), from 26.3's DataFixers: old data naming
 * {@code minecraft:chain} would otherwise fail to load.
 */
final class VanillaIds {
    static final Map<String, String> RENAMED = Map.of(
            "minecraft:grass", "minecraft:short_grass",               // DataFixers v3692 "Rename grass block/item to short_grass"
            "minecraft:scute", "minecraft:turtle_scute",              // v3800 "Rename scute item to turtle_scute"
            "minecraft:chain", "minecraft:iron_chain",                // v4541 "Rename chain to iron_chain"
            "minecraft:ocean_explorer_map", "minecraft:ocean_monument_map", // v5012 "Rename explorer map items"
            "minecraft:swamp_explorer_map", "minecraft:swamp_hut_map");

    /** Renames every string that is exactly an old id, in place. */
    static JsonElement rename(JsonElement e) {
        if (e instanceof JsonObject o) {
            for (var entry : o.entrySet()) entry.setValue(rename(entry.getValue()));
            return o;
        }
        if (e instanceof JsonArray a) {
            for (int i = 0; i < a.size(); i++) a.set(i, rename(a.get(i)));
            return a;
        }
        if (e instanceof JsonPrimitive p && p.isString()) {
            String renamed = RENAMED.get(p.getAsString());
            return renamed != null ? new JsonPrimitive(renamed) : p;
        }
        return e;
    }

    private VanillaIds() {}
}

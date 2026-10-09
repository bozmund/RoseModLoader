package rose.packfix.v1_20_1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;

/**
 * Upgrades 1.20.1 configured and placed features to 26.3. Evidence: vanilla's worldgen in both versions
 * (configured_feature/ in 1.20.1, feature/ in 26.3), e.g. {@code berry_bush}, {@code flower_default}:
 * <ul>
 *   <li>configured features moved to {@code worldgen/feature} and lost their {@code config} wrapper (vanilla types;
 *       mod types keep it, their 1.20.1 codecs read it, see the era's LegacyFeature);</li>
 *   <li>block states are {@code {id, properties}} instead of {@code {Name, Properties}};
 *       {@code simple_state_provider} became the plain state, other providers lost their {@code _state_provider} /
 *       {@code _provider} suffix;</li>
 *   <li>int providers dropped the {@code value} wrapper ({@code {type: uniform, min_inclusive, max_inclusive}}).</li>
 * </ul>
 * Not covered yet: {@code random_patch} / {@code flower} features (26.3 moved their tries and spread into the
 * placed feature as count + offset placements, which needs the placed feature that references them).
 */
final class WorldgenFix {
    private static final Set<String> INT_PROVIDERS = Set.of("minecraft:uniform", "minecraft:biased_to_bottom", "minecraft:clamped",
            "minecraft:clamped_normal", "minecraft:trapezoid");
    static final Set<String> UNSUPPORTED_FEATURES = Set.of("minecraft:random_patch", "minecraft:flower", "minecraft:no_bonemeal_flower");

    static JsonElement upgrade(JsonElement e) {
        if (e instanceof JsonArray a) {
            for (int i = 0; i < a.size(); i++) a.set(i, upgrade(a.get(i)));
            return a;
        }
        if (!(e instanceof JsonObject o)) return e;
        if (isBlockState(o)) return blockState(o);
        for (var entry : o.entrySet()) entry.setValue(upgrade(entry.getValue()));
        if (!o.has("type") || !o.get("type").isJsonPrimitive()) return o;
        String type = o.get("type").getAsString();
        if (!type.contains(":")) type = "minecraft:" + type;
        if (type.equals("minecraft:simple_state_provider") && o.has("state")) return o.get("state");
        if (type.endsWith("_state_provider")) o.addProperty("type", type = type.substring(0, type.length() - "_state_provider".length()));
        else if (type.endsWith("_provider")) o.addProperty("type", type = type.substring(0, type.length() - "_provider".length()));
        if (INT_PROVIDERS.contains(type) && o.get("value") instanceof JsonObject value) {
            o.remove("value");
            for (var v : value.entrySet()) o.add(v.getKey(), v.getValue());
        }
        if (type.startsWith("minecraft:") && o.get("config") instanceof JsonObject config) {
            o.remove("config");
            for (var c : config.entrySet()) o.add(c.getKey(), c.getValue());
        }
        return o;
    }

    private static boolean isBlockState(JsonObject o) {
        return o.has("Name") && o.get("Name").isJsonPrimitive() && List.of("Name", "Properties").containsAll(o.keySet());
    }

    private static JsonObject blockState(JsonObject o) {
        JsonObject state = new JsonObject();
        state.add("id", o.get("Name"));
        if (o.has("Properties")) state.add("properties", o.get("Properties"));
        return state;
    }

    private WorldgenFix() {}
}

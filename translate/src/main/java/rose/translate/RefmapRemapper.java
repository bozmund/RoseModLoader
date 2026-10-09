package rose.translate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import org.objectweb.asm.commons.Remapper;

/**
 * Rewrites a Mixin refmap so its targets use 26.3 names. Refmaps map a mixin's selectors to obfuscated targets,
 * e.g. {@code "isFood" -> "Lnet/minecraft/world/entity/animal/Rabbit;m_6898_(Lnet/minecraft/world/item/ItemStack;)Z"};
 * after translation the value must name {@code Rabbit.isFood(...)} instead.
 *
 * <p>Value forms: {@code Lowner;name(desc)ret} (method), {@code Lowner;name:desc} (field), {@code name(desc)}
 * (method on the mixin's target), or a class name.
 */
public final class RefmapRemapper {
    private final Remapper remapper;

    public RefmapRemapper(Remapper remapper) {
        this.remapper = remapper;
    }

    public String remap(String refmapJson) {
        JsonObject root = JsonParser.parseString(refmapJson).getAsJsonObject();
        if (root.has("mappings")) remapMappings(root.getAsJsonObject("mappings"));
        if (root.has("data")) {
            for (Map.Entry<String, JsonElement> environment : root.getAsJsonObject("data").entrySet()) {
                if (environment.getValue().isJsonObject()) remapMappings(environment.getValue().getAsJsonObject());
            }
        }
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root);
    }

    private void remapMappings(JsonObject mappings) {
        for (Map.Entry<String, JsonElement> mixin : mappings.entrySet()) {
            if (!mixin.getValue().isJsonObject()) continue;
            for (Map.Entry<String, JsonElement> entry : mixin.getValue().getAsJsonObject().entrySet()) {
                if (entry.getValue().isJsonPrimitive()) entry.setValue(new JsonPrimitive(remapReference(entry.getValue().getAsString())));
            }
        }
    }

    String remapReference(String ref) {
        String owner = null;
        String rest = ref;
        if (ref.startsWith("L") && ref.contains(";")) {
            owner = ref.substring(1, ref.indexOf(';'));
            rest = ref.substring(ref.indexOf(';') + 1);
        }
        if (rest.contains("(")) {
            String name = rest.substring(0, rest.indexOf('('));
            String desc = rest.substring(rest.indexOf('('));
            String newName = remapper.mapMethodName(owner != null ? owner : "", name, desc);
            return (owner != null ? "L" + remapper.map(owner) + ";" : "") + newName + remapper.mapMethodDesc(desc);
        }
        if (rest.contains(":")) {
            String name = rest.substring(0, rest.indexOf(':'));
            String desc = rest.substring(rest.indexOf(':') + 1);
            String newName = remapper.mapFieldName(owner != null ? owner : "", name, desc);
            return (owner != null ? "L" + remapper.map(owner) + ";" : "") + newName + ":" + remapper.mapDesc(desc);
        }
        if (owner != null) return "L" + remapper.map(owner) + ";" + rest;
        return ref.contains("/") ? remapper.map(ref) : ref;
    }
}

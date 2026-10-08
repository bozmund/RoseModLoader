package rose.bridge.methods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import rose.bridge.BridgeMethod;
import rose.bridge.BridgeServer;
import rose.bridge.EventLog;
import rose.loader.ModMetadata;
import rose.loader.RoseLoader;

/** Methods available on every side. */
public final class CommonMethods {
    public static void register(BridgeServer bridge) {
        bridge.register("rose.ping", "", "Liveness check. Returns side, Minecraft version, game directory and whether a server is running.",
                p -> {
                    JsonObject out = new JsonObject();
                    out.addProperty("side", bridge.side());
                    out.addProperty("minecraft", SharedConstants.getCurrentVersion().name());
                    out.addProperty("gameDir", Path.of("").toAbsolutePath().toString());
                    out.addProperty("serverRunning", rose.bridge.BridgeMod.serverOrNull() != null);
                    return out;
                });

        bridge.register("rose.methods", "", "Lists every bridge method with its parameters and description.", p -> {
            JsonArray out = new JsonArray();
            for (BridgeMethod method : bridge.methods().values()) {
                JsonObject m = new JsonObject();
                m.addProperty("name", method.name());
                m.addProperty("params", method.params());
                m.addProperty("description", method.description());
                out.add(m);
            }
            return out;
        });

        bridge.register("rose.mods", "", "Loaded mods: id, name, version, source jar/folder.", p -> {
            JsonArray out = new JsonArray();
            for (ModMetadata mod : RoseLoader.get().mods()) {
                JsonObject m = new JsonObject();
                m.addProperty("id", mod.id());
                m.addProperty("name", mod.name());
                m.addProperty("version", mod.version());
                m.addProperty("source", mod.root().toString());
                out.add(m);
            }
            return out;
        });

        bridge.register("events.poll", "after?:long, types?:[string], limit?:int",
                "Recent events after sequence number 'after' (default 0). Types: log, chat, overlay, player_join, "
                        + "server_started, server_stopping. Returns {events, lastSeq}; pass lastSeq as 'after' next time.",
                p -> {
                    Set<String> types = new HashSet<>();
                    for (JsonElement t : p.array("types")) types.add(t.getAsString());
                    return EventLog.poll(p.longValue("after", 0), types, p.integer("limit", 500));
                });

        bridge.register("registry.list", "registry:string (e.g. minecraft:block), namespace?:string",
                "Ids in a built-in registry, optionally only one namespace (e.g. a mod id).", p -> {
                    Identifier registryId = Identifier.parse(p.string("registry"));
                    Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
                    if (registry == null) throw new IllegalArgumentException("unknown registry " + registryId);
                    String namespace = p.string("namespace", null);
                    JsonArray out = new JsonArray();
                    registry.keySet().stream()
                            .filter(id -> namespace == null || id.getNamespace().equals(namespace))
                            .map(Identifier::toString).sorted().forEach(out::add);
                    return out;
                });
    }

    private CommonMethods() {}
}

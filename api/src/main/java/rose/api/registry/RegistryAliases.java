package rose.api.registry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * Old ids of renamed registry entries. A lookup of an id a registry doesn't have finds the entry it was renamed to,
 * so saved items, blocks and data written under the old id still load (Forge's {@code addAlias}; 26.3 has no
 * registry aliases, and vanilla renames go through DataFixers, which mods have none of).
 */
public final class RegistryAliases {
    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Identifier>> ALIASES = new ConcurrentHashMap<>();

    public static void add(ResourceKey<? extends Registry<?>> registry, Identifier from, Identifier to) {
        if (from.equals(to)) return;
        ALIASES.computeIfAbsent(registry, r -> new ConcurrentHashMap<>()).put(from, to);
    }

    /** The id {@code id} was renamed to, following chains of renames, or {@code null}; cycles have none. */
    public static @Nullable Identifier target(ResourceKey<? extends Registry<?>> registry, Identifier id) {
        Map<Identifier, Identifier> aliases = ALIASES.get(registry);
        if (aliases == null) return null;
        Identifier to = aliases.get(id);
        if (to == null) return null;
        Set<Identifier> seen = new HashSet<>();
        seen.add(id);
        while (aliases.containsKey(to)) {
            if (!seen.add(to)) return null;
            to = aliases.get(to);
        }
        return to;
    }

    private RegistryAliases() {}
}

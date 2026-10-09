package rose.era.v1_20_1;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import rose.era.v1_20_1.mixin.MappedRegistryAccess;
import rose.loader.RoseLoader;

/**
 * Data from translated (old) mods is read on a best-effort basis: an element that can't be loaded (a recipe for an
 * item the mod couldn't register, an old format Rose can't upgrade yet) is skipped and reported instead of stopping
 * the world from loading, which is what vanilla does for any registry error.
 */
public final class LegacyData {
    /** Elements skipped so far (e.g. {@code minecraft:recipe / mod:x}). */
    private static final Set<ResourceKey<?>> SKIPPED = ConcurrentHashMap.newKeySet();
    /** Marks a loading error from a translated mod's data, which the registry loader then only logs. */
    public static final class Skipped extends Exception {
        public Skipped(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Whether a pack ("mod/<id>") belongs to a mod translated from another loader or version. */
    public static boolean isLegacyPack(String packId) {
        if (packId == null || !packId.startsWith("mod/")) return false;
        return RoseLoader.get().mod(packId.substring(4)).map(m -> !m.isNative()).orElse(false);
    }

    /** A loading-errors map that drops (and logs) {@link Skipped} errors and keeps the rest. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <K> Map<K, Exception> tolerant(Map<K, Exception> errors) {
        return new java.util.AbstractMap<>() {
            @Override
            public Exception put(K key, Exception value) {
                if (value instanceof Skipped skipped) {
                    if (key instanceof ResourceKey<?> element) SKIPPED.add(element);
                    com.mojang.logging.LogUtils.getLogger().warn("[rose] skipped {}: {}", key, skipped.getMessage(), skipped.getCause());
                    return null;
                }
                return errors.put(key, value);
            }

            @Override
            public java.util.Set<Entry<K, Exception>> entrySet() {
                return errors.entrySet();
            }
        };
    }

    /**
     * Other data may name a skipped element (an advancement unlocking a skipped recipe); loading it left an unbound
     * placeholder that would fail the registry's freeze. Those placeholders are dropped: the references keep their
     * key (matching by key still works) and the element simply doesn't exist.
     */
    public static void dropSkippedPlaceholders(Registry<?> registry) {
        if (!(registry instanceof MappedRegistryAccess access)) return;
        access.rose$byKey().entrySet().removeIf(e -> {
            boolean drop = !e.getValue().isBound() && SKIPPED.contains(e.getKey());
            if (drop) {
                access.rose$byLocation().remove(e.getKey().identifier());
                com.mojang.logging.LogUtils.getLogger().warn("[rose] {} was skipped; references to it now point at nothing", e.getKey());
            }
            return drop;
        });
    }

    private LegacyData() {}
}

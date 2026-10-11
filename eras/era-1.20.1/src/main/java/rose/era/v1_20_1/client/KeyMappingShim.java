package rose.era.v1_20_1.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Redirect targets for 1.20.1 key mappings. 1.20.1 named a mapping's category by its translation key
 * ({@code "key.categories.misc"}, or a mod's own); 26.x has registered {@code KeyMapping.Category} values.
 */
public final class KeyMappingShim {
    private static final Map<String, KeyMapping.Category> BY_LEGACY = new ConcurrentHashMap<>();
    /** Mod categories by their 1.20.1 translation key, so their label keeps the mod's translation (KeyCategoryMixin). */
    public static final Map<KeyMapping.Category, String> LABELS = new ConcurrentHashMap<>();

    static {
        BY_LEGACY.put("key.categories.movement", KeyMapping.Category.MOVEMENT);
        BY_LEGACY.put("key.categories.misc", KeyMapping.Category.MISC);
        BY_LEGACY.put("key.categories.multiplayer", KeyMapping.Category.MULTIPLAYER);
        BY_LEGACY.put("key.categories.gameplay", KeyMapping.Category.GAMEPLAY);
        BY_LEGACY.put("key.categories.inventory", KeyMapping.Category.INVENTORY);
        BY_LEGACY.put("key.categories.creative", KeyMapping.Category.CREATIVE);
        BY_LEGACY.put("key.categories.ui", KeyMapping.Category.MISC);
    }

    /** 1.20.1 {@code new KeyMapping(name, keyCode, category)}. */
    public static KeyMapping keyMapping(String name, int keyCode, String category) {
        return new KeyMapping(name, keyCode, category(category));
    }

    /** 1.20.1 {@code new KeyMapping(name, type, keyCode, category)}. */
    public static KeyMapping keyMapping(String name, InputConstants.Type type, int keyCode, String category) {
        return new KeyMapping(name, type, keyCode, category(category));
    }

    /** The 26.3 category for a 1.20.1 category key, registered the first time a mod names it. */
    public static KeyMapping.Category category(String legacy) {
        return BY_LEGACY.computeIfAbsent(legacy, key -> {
            String path = key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
            KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("rose_legacy", path));
            LABELS.put(category, key);
            return category;
        });
    }

    private KeyMappingShim() {}
}

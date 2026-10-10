package rose.era.v1_20_1.enchantment;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import rose.era.v1_20_1.ExtraTags;

/**
 * Enchantments 1.20.1 mods registered in code. 26.3 loads enchantments from data into each server's registries, so
 * they are added there whenever the enchantment registry loads (RegistryLoadTaskMixin), unless a data pack defines
 * the same id. Each load builds the 26.3 records anew against that load's item tags.
 */
public final class LegacyEnchantments {
    private static final Map<ResourceKey<Enchantment>, LegacyEnchantment> ENTRIES = new LinkedHashMap<>();

    public static synchronized void register(ResourceKey<Enchantment> key, LegacyEnchantment legacy) {
        legacy.setKey(key);
        ENTRIES.put(key, legacy);
        // 1.20.1 placed enchantments by flags; 26.3 by enchantment tags.
        if (legacy.isTreasureOnly()) {
            tag(EnchantmentTags.TREASURE, key);
            tag(EnchantmentTags.DOUBLE_TRADE_PRICE, key);
        } else {
            tag(EnchantmentTags.NON_TREASURE, key);
            if (legacy.isDiscoverable()) tag(EnchantmentTags.IN_ENCHANTING_TABLE, key);
        }
        if (legacy.isDiscoverable()) tag(EnchantmentTags.ON_RANDOM_LOOT, key);
        if (legacy.isTradeable()) tag(EnchantmentTags.TRADEABLE, key);
        if (legacy.isCurse()) tag(EnchantmentTags.CURSE, key);
    }

    private static void tag(TagKey<Enchantment> tag, ResourceKey<Enchantment> key) {
        ExtraTags.addElement(tag, key.identifier());
    }

    /**
     * Adds the registered enchantments to a loading enchantment registry (before its tags load). {@code items} is
     * the load's item lookup, with the item tags of the data being loaded.
     */
    @SuppressWarnings("unchecked")
    public static synchronized void addTo(WritableRegistry<?> registry, HolderGetter<Item> items) {
        if (!registry.key().equals(Registries.ENCHANTMENT) || ENTRIES.isEmpty()) return;
        WritableRegistry<Enchantment> enchantments = (WritableRegistry<Enchantment>) registry;
        ENTRIES.forEach((key, legacy) -> {
            if (enchantments.containsKey(key)) return; // a data pack defines it
            enchantments.register(key, legacy.toEnchantment(key.identifier(), items), RegistrationInfo.BUILT_IN);
        });
    }

    private LegacyEnchantments() {}
}

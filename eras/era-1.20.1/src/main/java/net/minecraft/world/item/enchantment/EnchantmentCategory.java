package net.minecraft.world.item.enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Era bridge (1.20.1): which items an enchantment applies to. 1.21 replaced these categories with item tags
 * ({@code #minecraft:enchantable/...}); each vanilla category checks the matching tag. Forge let mods add categories
 * ({@link #create}), so this is a class with constants rather than a closed enum.
 */
public final class EnchantmentCategory {
    private static final List<EnchantmentCategory> VALUES = new ArrayList<>();

    public static final EnchantmentCategory ARMOR = of("ARMOR", ItemTags.ARMOR_ENCHANTABLE);
    public static final EnchantmentCategory ARMOR_FEET = of("ARMOR_FEET", ItemTags.FOOT_ARMOR_ENCHANTABLE);
    public static final EnchantmentCategory ARMOR_LEGS = of("ARMOR_LEGS", ItemTags.LEG_ARMOR_ENCHANTABLE);
    public static final EnchantmentCategory ARMOR_CHEST = of("ARMOR_CHEST", ItemTags.CHEST_ARMOR_ENCHANTABLE);
    public static final EnchantmentCategory ARMOR_HEAD = of("ARMOR_HEAD", ItemTags.HEAD_ARMOR_ENCHANTABLE);
    public static final EnchantmentCategory WEAPON = of("WEAPON", ItemTags.SWORDS);
    public static final EnchantmentCategory DIGGER = of("DIGGER", ItemTags.MINING_ENCHANTABLE);
    public static final EnchantmentCategory FISHING_ROD = of("FISHING_ROD", ItemTags.FISHING_ENCHANTABLE);
    public static final EnchantmentCategory TRIDENT = of("TRIDENT", ItemTags.TRIDENT_ENCHANTABLE);
    public static final EnchantmentCategory BREAKABLE = of("BREAKABLE", ItemTags.DURABILITY_ENCHANTABLE);
    public static final EnchantmentCategory BOW = of("BOW", ItemTags.BOW_ENCHANTABLE);
    public static final EnchantmentCategory WEARABLE = of("WEARABLE", ItemTags.EQUIPPABLE_ENCHANTABLE);
    public static final EnchantmentCategory CROSSBOW = of("CROSSBOW", ItemTags.CROSSBOW_ENCHANTABLE);
    public static final EnchantmentCategory VANISHABLE = of("VANISHABLE", ItemTags.VANISHING_ENCHANTABLE);

    private final String name;
    private final int ordinal;
    private final Predicate<Item> delegate;
    private final TagKey<Item> tag;

    private EnchantmentCategory(String name, Predicate<Item> delegate, TagKey<Item> tag) {
        this.name = name;
        this.ordinal = VALUES.size();
        this.delegate = delegate;
        this.tag = tag;
        VALUES.add(this);
    }

    private static EnchantmentCategory of(String name, TagKey<Item> tag) {
        return new EnchantmentCategory(name, item -> item.builtInRegistryHolder().is(tag), tag);
    }

    /** Forge: a new category for items matching {@code delegate}. */
    public static EnchantmentCategory create(String name, Predicate<Item> delegate) {
        return new EnchantmentCategory(name, delegate, null);
    }

    /**
     * Rose: the items as a 26.3 enchantment's {@code supported_items}: the category's tag (resolved through
     * {@code items}, the loading server's view of item tags), or the items its predicate accepts.
     */
    public HolderSet<Item> rose$items(HolderGetter<Item> items) {
        if (tag != null) return items.getOrThrow(tag);
        List<Holder<Item>> out = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (delegate.test(item)) out.add(item.builtInRegistryHolder());
        }
        return HolderSet.direct(out);
    }

    public boolean canEnchant(Item item) {
        return delegate.test(item);
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }

    public static EnchantmentCategory[] values() {
        return VALUES.toArray(EnchantmentCategory[]::new);
    }

    @Override
    public String toString() {
        return name;
    }
}

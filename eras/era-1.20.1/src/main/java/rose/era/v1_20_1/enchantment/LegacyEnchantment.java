package rose.era.v1_20_1.enchantment;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment$Rarity;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import rose.era.v1_20_1.EraContext;

/**
 * Era bridge (1.20.1): the {@code Enchantment} class of 1.20.1 code (class-renamed; since 1.21 enchantments are a
 * final, data-driven record loaded into each server's registry). Mod enchantments extend it and describe themselves
 * through the 1.20.1 methods; {@link #toEnchantment} turns that into the 26.3 record LegacyEnchantments adds to the
 * registry. Vanilla enchantments ({@code Enchantments.X}) are {@link #of views} that answer from the registry.
 */
public class LegacyEnchantment {
    private static final Map<ResourceKey<Enchantment>, LegacyEnchantment> VANILLA = new ConcurrentHashMap<>();

    public final EnchantmentCategory category;
    private final Enchantment$Rarity rarity;
    private final EquipmentSlot[] slots;
    private volatile ResourceKey<Enchantment> key;

    protected LegacyEnchantment(Enchantment$Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        this.rarity = rarity;
        this.category = category;
        this.slots = slots;
    }

    /** The 1.20.1 view of a registry enchantment (one instance per key, so {@code ==} works as it did). */
    public static LegacyEnchantment of(ResourceKey<Enchantment> key) {
        return VANILLA.computeIfAbsent(key, Vanilla::new);
    }

    void setKey(ResourceKey<Enchantment> key) {
        this.key = key;
    }

    public ResourceKey<Enchantment> key() {
        return key;
    }

    /** This enchantment in the running server's registry. */
    public Holder<Enchantment> holder() {
        if (key == null) throw new IllegalStateException("enchantment " + this + " was never registered");
        Registry<Enchantment> registry = EraContext.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return registry.getOrThrow(key);
    }

    public Enchantment$Rarity getRarity() {
        return rarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinCost(int level) {
        return 1 + level * 10;
    }

    public int getMaxCost(int level) {
        return getMinCost(level) + 5;
    }

    public final boolean isCompatibleWith(LegacyEnchantment other) {
        return checkCompatibility(other) && other.checkCompatibility(this);
    }

    protected boolean checkCompatibility(LegacyEnchantment other) {
        return this != other;
    }

    public String getDescriptionId() {
        return key == null ? "enchantment.unregistered" : net.minecraft.util.Util.makeDescriptionId("enchantment", key.identifier());
    }

    public Component getFullname(int level) {
        return Enchantment.getFullname(holder(), level);
    }

    public boolean canEnchant(ItemStack stack) {
        return category.canEnchant(stack.getItem());
    }

    /** Forge: whether the enchanting table offers it for {@code stack}. */
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public boolean isTradeable() {
        return true;
    }

    public boolean isDiscoverable() {
        return true;
    }

    /** Forge: whether it can go on enchanted books. */
    public boolean isAllowedOnBooks() {
        return true;
    }

    /**
     * The 26.3 enchantment this describes, with item tags resolved through {@code items} (the loading server's view).
     * Its items are its category's: item components aren't bound while registries load, so a mod's own
     * {@code canEnchant(ItemStack)} can't be asked then. Costs become linear (1.20.1 ones are linear in practice); the
     * anvil cost follows the rarity as 1.20.1's anvil did. Behavior in overridden methods (damage bonus, post-attack)
     * isn't carried over: mods mostly did that in event listeners, which still run.
     */
    public Enchantment toEnchantment(Identifier id, HolderGetter<Item> items) {
        int minBase = getMinCost(1);
        int maxBase = getMaxCost(1);
        Enchantment.EnchantmentDefinition definition = Enchantment.definition(category.rose$items(items),
                rarity.getWeight(), getMaxLevel(), Enchantment.dynamicCost(minBase, getMinCost(2) - minBase),
                Enchantment.dynamicCost(maxBase, getMaxCost(2) - maxBase), anvilCost(), slotGroups());
        return Enchantment.enchantment(definition).build(id);
    }

    private int anvilCost() {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 4;
            case VERY_RARE -> 8;
        };
    }

    private EquipmentSlotGroup[] slotGroups() {
        Set<EquipmentSlotGroup> groups = new LinkedHashSet<>();
        for (EquipmentSlot slot : slots) groups.add(EquipmentSlotGroup.bySlot(slot));
        return groups.toArray(EquipmentSlotGroup[]::new);
    }

    @Override
    public String toString() {
        return key == null ? getClass().getName() : key.identifier().toString();
    }

    /** A registry enchantment seen through the 1.20.1 API. */
    private static final class Vanilla extends LegacyEnchantment {
        Vanilla(ResourceKey<Enchantment> key) {
            super(Enchantment$Rarity.COMMON, null, new EquipmentSlot[0]);
            setKey(key);
        }

        private Enchantment value() {
            return holder().value();
        }

        @Override
        public Enchantment$Rarity getRarity() {
            int weight = value().getWeight();
            for (Enchantment$Rarity r : Enchantment$Rarity.values()) {
                if (r.getWeight() <= weight) return r;
            }
            return Enchantment$Rarity.VERY_RARE;
        }

        @Override
        public int getMinLevel() {
            return value().getMinLevel();
        }

        @Override
        public int getMaxLevel() {
            return value().getMaxLevel();
        }

        @Override
        public int getMinCost(int level) {
            return value().getMinCost(level);
        }

        @Override
        public int getMaxCost(int level) {
            return value().getMaxCost(level);
        }

        @Override
        protected boolean checkCompatibility(LegacyEnchantment other) {
            return other.key() == null || Enchantment.areCompatible(holder(), other.holder());
        }

        @Override
        public boolean canEnchant(ItemStack stack) {
            return value().canEnchant(stack);
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return value().isPrimaryItem(stack);
        }

        @Override
        public boolean isTreasureOnly() {
            return holder().is(EnchantmentTags.TREASURE);
        }

        @Override
        public boolean isCurse() {
            return holder().is(EnchantmentTags.CURSE);
        }

        @Override
        public boolean isTradeable() {
            return holder().is(EnchantmentTags.TRADEABLE);
        }

        @Override
        public boolean isDiscoverable() {
            return holder().is(EnchantmentTags.ON_RANDOM_LOOT);
        }
    }
}

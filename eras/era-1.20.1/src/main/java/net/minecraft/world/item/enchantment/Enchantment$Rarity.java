package net.minecraft.world.item.enchantment;

/** Era bridge (1.20.1): enchantment rarity (weight). 1.21 data-driven enchantments have a numeric weight. */
public enum Enchantment$Rarity {
    COMMON(10),
    UNCOMMON(5),
    RARE(2),
    VERY_RARE(1);

    private final int weight;

    Enchantment$Rarity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}

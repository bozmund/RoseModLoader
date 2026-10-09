package net.minecraft.world.item;

import rose.era.v1_20_1.LegacyTools;

/** Era bridge (1.20.1): an item made of a tool tier (durability, enchantability, repair item). */
public class TieredItem extends Item {
    private final Tier tier;

    public TieredItem(Tier tier, Item.Properties properties) {
        super(LegacyTools.tiered(tier, properties));
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }
}

package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import rose.era.v1_20_1.LegacyTools;

/**
 * Era bridge (1.20.1): a mining tool. In 26.3 tools are plain items with a {@code tool} component and attribute
 * modifiers; this sets those up from the 1.20.1 constructor arguments.
 */
public class DiggerItem extends TieredItem {
    public DiggerItem(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Item.Properties properties) {
        super(tier, LegacyTools.digger(tier, blocks, attackDamageModifier, attackSpeedModifier, properties));
    }
}

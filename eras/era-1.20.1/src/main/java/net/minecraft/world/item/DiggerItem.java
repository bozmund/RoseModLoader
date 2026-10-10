package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

    /** 1.20.1 DiggerItem.hurtEnemy: hitting wears the tool by 2 and counts as a use. */
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
        rose.era.v1_20_1.bridge.ItemBridges.countUse(this, stack, attacker);
    }
}

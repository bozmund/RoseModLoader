package rose.era.v1_20_1;

import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.block.Block;

/** Turns 1.20.1 tool tiers into 26.3 item properties (what ToolMaterial.applyToolProperties does for vanilla). */
public final class LegacyTools {
    /** 1.20.1 TieredItem: durability from the tier, enchantability, repair ingredient. */
    public static Item.Properties tiered(Tier tier, Item.Properties properties) {
        properties.durability(tier.getUses()).enchantable(tier.getEnchantmentValue());
        if (tier instanceof Tiers vanilla) return properties.repairable(vanilla.material().repairItems());
        try {
            var items = HolderSet.direct(tier.getRepairIngredient().items().toList());
            properties.component(DataComponents.REPAIRABLE, new Repairable(items));
        } catch (RuntimeException tagsNotLoadedYet) {
            // Tag-based repair ingredients can't be resolved while registries are open; the item just isn't repairable.
        }
        return properties;
    }

    /**
     * 1.20.1 DiggerItem: mines {@code blocks} at the tier's speed, attack damage = modifier + tier bonus. No
     * {@code weapon} component: hitting wears it through its own {@code hurtEnemy}, as in 1.20.1, so a subclass that
     * overrides it decides the wear.
     */
    public static Item.Properties digger(Tier tier, TagKey<Block> blocks, float attackDamageModifier, float attackSpeedModifier,
                                        Item.Properties properties) {
        var lookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        Tool tool = new Tool(List.of(
                Tool.Rule.deniesDrops(lookup.getOrThrow(incorrectFor(tier))),
                Tool.Rule.minesAndDrops(lookup.getOrThrow(blocks), tier.getSpeed())), 1.0F, 1, true);
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                        attackDamageModifier + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                        attackSpeedModifier, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
        return properties.component(DataComponents.TOOL, tool).attributes(attributes);
    }

    /** The blocks a tier can't harvest: Forge's per-tier tag, else vanilla's tag for the tier's mining level. */
    static TagKey<Block> incorrectFor(Tier tier) {
        if (tier.getTag() != null) return tier.getTag();
        if (tier instanceof Tiers vanilla) return vanilla.material().incorrectBlocksForDrops();
        return switch (tier.getLevel()) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    private LegacyTools() {}
}

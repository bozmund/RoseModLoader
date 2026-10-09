package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Era bridge (1.20.1): a tool material. 26.3 replaced it with the ToolMaterial record (see LegacyTools). */
public interface Tier {
    int getUses();

    float getSpeed();

    float getAttackDamageBonus();

    int getLevel();

    int getEnchantmentValue();

    Ingredient getRepairIngredient();

    /** Forge: the blocks this tier can't mine; {@code null} uses the vanilla tag for the tier's level. */
    default TagKey<Block> getTag() {
        return null;
    }
}

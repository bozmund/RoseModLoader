package net.minecraft.world.item;

import java.util.function.Supplier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Era bridge (1.20.1): the vanilla tool tiers, with their 1.20.1 values (same numbers as 26.3's ToolMaterial, which
 * {@link #material()} names: tools made of a vanilla tier get its 26.3 repair tag and harvest tag).
 */
public enum Tiers implements Tier {
    WOOD(0, 59, 2.0F, 0.0F, 15, () -> Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(ItemTags.PLANKS)), ToolMaterial.WOOD),
    STONE(1, 131, 4.0F, 1.0F, 5, () -> Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(ItemTags.STONE_TOOL_MATERIALS)), ToolMaterial.STONE),
    IRON(2, 250, 6.0F, 2.0F, 14, () -> Ingredient.of(Items.IRON_INGOT), ToolMaterial.IRON),
    DIAMOND(3, 1561, 8.0F, 3.0F, 10, () -> Ingredient.of(Items.DIAMOND), ToolMaterial.DIAMOND),
    GOLD(0, 32, 12.0F, 0.0F, 22, () -> Ingredient.of(Items.GOLD_INGOT), ToolMaterial.GOLD),
    NETHERITE(4, 2031, 9.0F, 4.0F, 15, () -> Ingredient.of(Items.NETHERITE_INGOT), ToolMaterial.NETHERITE);

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;
    private final ToolMaterial material;

    Tiers(int level, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient, ToolMaterial material) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
        this.material = material;
    }

    /** The 26.3 tool material this tier became. */
    public ToolMaterial material() {
        return material;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}

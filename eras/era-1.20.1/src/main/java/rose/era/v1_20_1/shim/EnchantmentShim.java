package rose.era.v1_20_1.shim;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import rose.era.v1_20_1.EraContext;
import rose.era.v1_20_1.enchantment.LegacyEnchantment;

/**
 * 1.21 made enchantments data-driven: {@code Enchantments} holds keys and the values live in the server's dynamic
 * registry. 1.20.1 code reads {@code Enchantments.X} as a value and passes it around; it gets a LegacyEnchantment
 * view, and the shims look it up in the running server's registry.
 */
public final class EnchantmentShim {
    public static LegacyEnchantment sharpness() { return LegacyEnchantment.of(Enchantments.SHARPNESS); }
    public static LegacyEnchantment smite() { return LegacyEnchantment.of(Enchantments.SMITE); }
    public static LegacyEnchantment baneOfArthropods() { return LegacyEnchantment.of(Enchantments.BANE_OF_ARTHROPODS); }
    public static LegacyEnchantment knockback() { return LegacyEnchantment.of(Enchantments.KNOCKBACK); }
    public static LegacyEnchantment fireAspect() { return LegacyEnchantment.of(Enchantments.FIRE_ASPECT); }
    public static LegacyEnchantment looting() { return LegacyEnchantment.of(Enchantments.LOOTING); }
    public static LegacyEnchantment sweepingEdge() { return LegacyEnchantment.of(Enchantments.SWEEPING_EDGE); }
    public static LegacyEnchantment fortune() { return LegacyEnchantment.of(Enchantments.FORTUNE); }
    public static LegacyEnchantment protection() { return LegacyEnchantment.of(Enchantments.PROTECTION); }
    public static LegacyEnchantment fireProtection() { return LegacyEnchantment.of(Enchantments.FIRE_PROTECTION); }
    public static LegacyEnchantment featherFalling() { return LegacyEnchantment.of(Enchantments.FEATHER_FALLING); }
    public static LegacyEnchantment blastProtection() { return LegacyEnchantment.of(Enchantments.BLAST_PROTECTION); }
    public static LegacyEnchantment projectileProtection() { return LegacyEnchantment.of(Enchantments.PROJECTILE_PROTECTION); }
    public static LegacyEnchantment respiration() { return LegacyEnchantment.of(Enchantments.RESPIRATION); }
    public static LegacyEnchantment aquaAffinity() { return LegacyEnchantment.of(Enchantments.AQUA_AFFINITY); }
    public static LegacyEnchantment thorns() { return LegacyEnchantment.of(Enchantments.THORNS); }
    public static LegacyEnchantment depthStrider() { return LegacyEnchantment.of(Enchantments.DEPTH_STRIDER); }
    public static LegacyEnchantment frostWalker() { return LegacyEnchantment.of(Enchantments.FROST_WALKER); }
    public static LegacyEnchantment bindingCurse() { return LegacyEnchantment.of(Enchantments.BINDING_CURSE); }
    public static LegacyEnchantment soulSpeed() { return LegacyEnchantment.of(Enchantments.SOUL_SPEED); }
    public static LegacyEnchantment swiftSneak() { return LegacyEnchantment.of(Enchantments.SWIFT_SNEAK); }
    public static LegacyEnchantment efficiency() { return LegacyEnchantment.of(Enchantments.EFFICIENCY); }
    public static LegacyEnchantment silkTouch() { return LegacyEnchantment.of(Enchantments.SILK_TOUCH); }
    public static LegacyEnchantment unbreaking() { return LegacyEnchantment.of(Enchantments.UNBREAKING); }
    public static LegacyEnchantment power() { return LegacyEnchantment.of(Enchantments.POWER); }
    public static LegacyEnchantment punch() { return LegacyEnchantment.of(Enchantments.PUNCH); }
    public static LegacyEnchantment flame() { return LegacyEnchantment.of(Enchantments.FLAME); }
    public static LegacyEnchantment infinity() { return LegacyEnchantment.of(Enchantments.INFINITY); }
    public static LegacyEnchantment luckOfTheSea() { return LegacyEnchantment.of(Enchantments.LUCK_OF_THE_SEA); }
    public static LegacyEnchantment lure() { return LegacyEnchantment.of(Enchantments.LURE); }
    public static LegacyEnchantment loyalty() { return LegacyEnchantment.of(Enchantments.LOYALTY); }
    public static LegacyEnchantment impaling() { return LegacyEnchantment.of(Enchantments.IMPALING); }
    public static LegacyEnchantment riptide() { return LegacyEnchantment.of(Enchantments.RIPTIDE); }
    public static LegacyEnchantment channeling() { return LegacyEnchantment.of(Enchantments.CHANNELING); }
    public static LegacyEnchantment multishot() { return LegacyEnchantment.of(Enchantments.MULTISHOT); }
    public static LegacyEnchantment quickCharge() { return LegacyEnchantment.of(Enchantments.QUICK_CHARGE); }
    public static LegacyEnchantment piercing() { return LegacyEnchantment.of(Enchantments.PIERCING); }
    public static LegacyEnchantment mending() { return LegacyEnchantment.of(Enchantments.MENDING); }
    public static LegacyEnchantment vanishingCurse() { return LegacyEnchantment.of(Enchantments.VANISHING_CURSE); }

    /** Conversion {@code Enchantment -> Holder} for 26.3 APIs that take {@code Holder<Enchantment>}. */
    public static Holder<Enchantment> holder(LegacyEnchantment enchantment) {
        if (EraContext.server() == null) throw new IllegalStateException("Enchantments live in the server's registries; no server is running");
        return enchantment.holder();
    }

    /** 1.20.1 {@code EnchantmentHelper.getItemEnchantmentLevel} and Forge {@code getTagEnchantmentLevel}: the level on the stack. */
    public static int getTagEnchantmentLevel(LegacyEnchantment enchantment, ItemStack stack) {
        return enchantment == null || EraContext.server() == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(enchantment.holder(), stack);
    }

    /** Forge 1.20.1 {@code ItemStack.getEnchantmentLevel(enchantment)}: the level on the stack. */
    public static int getEnchantmentLevel(ItemStack stack, LegacyEnchantment enchantment) {
        return getTagEnchantmentLevel(enchantment, stack);
    }

    private EnchantmentShim() {}

    /** 1.20.1 {@code EnchantmentHelper.hasFrostWalker(entity)}: the entity wears Frost Walker (enchantments are data since 1.21). */
    public static boolean hasFrostWalker(LivingEntity entity) {
        return entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.FROST_WALKER)
                .map(frostWalker -> EnchantmentHelper.getEnchantmentLevel(frostWalker, entity) > 0)
                .orElse(false);
    }
}

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

    /** Conversion {@code Enchantment -> Holder} for 26.3 APIs that take {@code Holder<Enchantment>}. */
    public static Holder<Enchantment> holder(LegacyEnchantment enchantment) {
        if (EraContext.server() == null) throw new IllegalStateException("Enchantments live in the server's registries; no server is running");
        return enchantment.holder();
    }

    /** 1.20.1 {@code EnchantmentHelper.getItemEnchantmentLevel} and Forge {@code getTagEnchantmentLevel}: the level on the stack. */
    public static int getTagEnchantmentLevel(LegacyEnchantment enchantment, ItemStack stack) {
        return enchantment == null || EraContext.server() == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(enchantment.holder(), stack);
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

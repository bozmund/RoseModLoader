package rose.era.v1_20_1.shim;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import rose.era.v1_20_1.EraContext;

/**
 * 1.21 made enchantments data-driven: {@code Enchantments} holds keys and the values live in the server's dynamic
 * registry. 1.20.1 code reads {@code Enchantments.X} as a value and passes it around, so the shim looks it up in the
 * running server's registry ({@code null} when no server is running).
 */
public final class EnchantmentShim {
    public static Enchantment sharpness() { return value(Enchantments.SHARPNESS); }
    public static Enchantment smite() { return value(Enchantments.SMITE); }
    public static Enchantment baneOfArthropods() { return value(Enchantments.BANE_OF_ARTHROPODS); }
    public static Enchantment knockback() { return value(Enchantments.KNOCKBACK); }
    public static Enchantment fireAspect() { return value(Enchantments.FIRE_ASPECT); }
    public static Enchantment looting() { return value(Enchantments.LOOTING); }
    public static Enchantment sweepingEdge() { return value(Enchantments.SWEEPING_EDGE); }
    public static Enchantment fortune() { return value(Enchantments.FORTUNE); }

    public static Enchantment value(ResourceKey<Enchantment> key) {
        Registry<Enchantment> registry = registry();
        return registry == null ? null : registry.getValue(key);
    }

    /** Conversion {@code Enchantment -> Holder} for 26.3 APIs that take {@code Holder<Enchantment>}. */
    public static Holder<Enchantment> holder(Enchantment value) {
        Registry<Enchantment> registry = registry();
        if (registry == null) throw new IllegalStateException("Enchantments live in the server's registries; no server is running");
        return registry.wrapAsHolder(value);
    }

    /** Forge {@code EnchantmentHelper.getTagEnchantmentLevel(enchantment, stack)}: the level on the stack itself. */
    public static int getTagEnchantmentLevel(Enchantment enchantment, ItemStack stack) {
        return enchantment == null || registry() == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder(enchantment), stack);
    }

    private static Registry<Enchantment> registry() {
        return EraContext.server() == null ? null : EraContext.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }

    private EnchantmentShim() {}
}

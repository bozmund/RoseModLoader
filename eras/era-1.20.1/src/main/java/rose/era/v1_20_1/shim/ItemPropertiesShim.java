package rose.era.v1_20_1.shim;

import net.minecraft.world.item.Item;

/** Redirect targets for 1.20.1 {@code Item.Properties} methods. */
public final class ItemPropertiesShim {
    /** 1.20.1 {@code defaultDurability(n)}: durability unless one was set already; 26.3 has only durability(n). */
    public static Item.Properties defaultDurability(Item.Properties self, int maxDamage) {
        return self.durability(maxDamage);
    }

    private ItemPropertiesShim() {}
}

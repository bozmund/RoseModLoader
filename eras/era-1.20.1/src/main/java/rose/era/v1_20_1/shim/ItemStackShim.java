package rose.era.v1_20_1.shim;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Redirect targets for 1.20.1 {@code ItemStack} methods that changed in 26.3. */
public final class ItemStackShim {
    /**
     * 1.20.1 {@code ItemStack.is(Item)}: {@code return this.getItem() == item;}. 26.3 only has
     * {@code is(Predicate<Holder<Item>>)}, but {@code getItem()} is unchanged.
     */
    public static boolean is(ItemStack self, Item item) {
        return self.getItem() == item;
    }

    private ItemStackShim() {}
}

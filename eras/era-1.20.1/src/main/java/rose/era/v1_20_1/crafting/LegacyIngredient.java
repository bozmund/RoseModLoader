package rose.era.v1_20_1.crafting;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient$Value;

/**
 * Era bridge (1.20.1): Ingredient as 1.20.1 mods extend it (a stream of values; subclasses usually override test).
 * 26.3 keeps an ingredient's items in a HolderSet, which vanilla uses for display and syncing; matching goes
 * through test(), which old subclasses override. Mod subclasses of Ingredient are re-parented here.
 */
public class LegacyIngredient extends Ingredient {
    private final List<ItemStack> items;

    protected LegacyIngredient(Stream<? extends Ingredient$Value> values) {
        this(values.flatMap(v -> v.getItems().stream()).toList());
    }

    private LegacyIngredient(List<ItemStack> items) {
        super(holders(items));
        this.items = items;
    }

    /** Vanilla requires a non-empty set without air; an empty legacy ingredient shows as a barrier. */
    private static HolderSet<Item> holders(List<ItemStack> items) {
        List<Holder<Item>> holders = items.stream().filter(s -> !s.isEmpty()).map(ItemStack::typeHolder).distinct().toList();
        return holders.isEmpty() ? HolderSet.direct(Items.BARRIER.builtInRegistryHolder()) : HolderSet.direct(holders);
    }

    /** 1.20.1 {@code getItems()}: the matching stacks, for display. */
    public ItemStack[] getItems() {
        return items.toArray(ItemStack[]::new);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}

package net.minecraft.world.item.crafting;

import java.util.Collection;
import net.minecraft.world.item.ItemStack;

/** Era bridge (1.20.1): one alternative of an ingredient (an item or a tag). 26.3 ingredients are a HolderSet. */
public interface Ingredient$Value {
    Collection<ItemStack> getItems();
}

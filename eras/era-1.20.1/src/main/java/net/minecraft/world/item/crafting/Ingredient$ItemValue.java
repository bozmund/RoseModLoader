package net.minecraft.world.item.crafting;

import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Era bridge (1.20.1): an ingredient alternative matching one item. */
public record Ingredient$ItemValue(ItemStack item) implements Ingredient$Value {
    @Override
    public Collection<ItemStack> getItems() {
        return List.of(item);
    }
}

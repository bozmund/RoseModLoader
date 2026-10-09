package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Era bridge (1.20.1): an ingredient alternative matching an item tag. */
public record Ingredient$TagValue(TagKey<Item> tag) implements Ingredient$Value {
    @Override
    public Collection<ItemStack> getItems() {
        List<ItemStack> out = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) out.add(new ItemStack(holder));
        return out;
    }
}

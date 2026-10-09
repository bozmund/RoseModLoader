package net.minecraft.world.item;

import net.minecraft.world.level.block.Block;

/**
 * Era bridge (1.20.1): a block item named after the item, not the block (seeds). Removed in 1.21.2, where
 * {@code Item.Properties.useItemDescriptionPrefix()} does the same on a plain BlockItem.
 */
public class ItemNameBlockItem extends BlockItem {
    public ItemNameBlockItem(Block block, Item.Properties properties) {
        super(block, properties.useItemDescriptionPrefix());
    }
}

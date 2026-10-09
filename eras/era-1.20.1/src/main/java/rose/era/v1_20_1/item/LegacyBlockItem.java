package rose.era.v1_20_1.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Era bridge (1.20.1): mod subclasses of BlockItem are named after their block, as 1.20.1's
 * {@code BlockItem.getDescriptionId()} did. 26.3 takes the name from {@code Item.Properties}.
 */
public class LegacyBlockItem extends BlockItem {
    public LegacyBlockItem(Block block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }
}

package rose.era.v1_20_1.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Era bridge (1.20.1): mod subclasses of BlockItem are named after their block, as 1.20.1's
 * {@code BlockItem.getDescriptionId()} did. 26.3 takes the name from {@code Item.Properties}.
 */
public class LegacyBlockItem extends BlockItem {
    public LegacyBlockItem(Block block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    /**
     * 1.20.1's instance hook after a block is placed (mods override it, e.g. FD's skillet keeps its item); 26.x only
     * has the static {@code updateCustomBlockEntityTag(level, player, pos, stack)}, which this calls as 1.20.1 did.
     * BlockItemMixin calls it from {@code place}.
     */
    public boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        return BlockItem.updateCustomBlockEntityTag(level, player, pos, stack);
    }
}

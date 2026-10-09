package net.minecraftforge.common;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Blocks and entities that shears can harvest. */
public interface IForgeShearable {
    default boolean isShearable(ItemStack item, Level level, BlockPos pos) {
        return true;
    }

    default List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
        return Collections.emptyList();
    }
}

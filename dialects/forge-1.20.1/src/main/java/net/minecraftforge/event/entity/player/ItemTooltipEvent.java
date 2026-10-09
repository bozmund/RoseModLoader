package net.minecraftforge.event.entity.player;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A tooltip is being built; listeners may add lines. Client only. */
public class ItemTooltipEvent extends PlayerEvent {
    private final TooltipFlag flags;
    private final ItemStack itemStack;
    private final List<Component> toolTip;

    public ItemTooltipEvent(ItemStack itemStack, Player player, List<Component> list, TooltipFlag flags) {
        super(player);
        this.itemStack = itemStack;
        this.toolTip = list;
        this.flags = flags;
    }

    public TooltipFlag getFlags() {
        return flags;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getToolTip() {
        return toolTip;
    }
}

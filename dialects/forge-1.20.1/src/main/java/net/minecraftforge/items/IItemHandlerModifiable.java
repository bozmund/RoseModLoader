package net.minecraftforge.items;

import net.minecraft.world.item.ItemStack;

/** An item handler whose slots can be set directly. */
public interface IItemHandlerModifiable extends IItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}

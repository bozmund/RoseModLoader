package net.minecraftforge.items;

import net.minecraft.world.item.ItemStack;

/** A slotted inventory that other code can insert into and extract from. */
public interface IItemHandler {
    int getSlots();

    ItemStack getStackInSlot(int slot);

    /** Returns what could not be inserted. {@code simulate} leaves the handler unchanged. */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}

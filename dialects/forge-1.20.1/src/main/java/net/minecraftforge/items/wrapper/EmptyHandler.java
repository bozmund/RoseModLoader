package net.minecraftforge.items.wrapper;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/** An item handler with no slots. */
public class EmptyHandler implements IItemHandlerModifiable {
    public static final IItemHandlerModifiable INSTANCE = new EmptyHandler();

    @Override
    public int getSlots() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        // nothing to set
    }
}

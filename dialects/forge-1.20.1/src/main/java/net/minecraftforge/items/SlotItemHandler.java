package net.minecraftforge.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A menu slot backed by an {@link IItemHandler} instead of a vanilla {@link Container}. */
public class SlotItemHandler extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(EMPTY, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        ((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
        setChanged();
    }

    public void initialize(ItemStack stack) {
        ((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
        setChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {}

    @Override
    public int getMaxStackSize() {
        return itemHandler.getSlotLimit(index);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        ItemStack maxAdd = stack.copyWithCount(stack.getMaxStackSize());
        IItemHandler handler = getItemHandler();
        ItemStack current = handler.getStackInSlot(index);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(index, ItemStack.EMPTY);
            ItemStack remainder = modifiable.insertItem(index, maxAdd, true);
            modifiable.setStackInSlot(index, current);
            return maxAdd.getCount() - remainder.getCount();
        }
        ItemStack remainder = handler.insertItem(index, maxAdd, true);
        return current.getCount() + maxAdd.getCount() - remainder.getCount();
    }

    @Override
    public boolean mayPickup(Player player) {
        return !itemHandler.extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return itemHandler.extractItem(index, amount, false);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }
}

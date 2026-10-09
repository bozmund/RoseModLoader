package net.minecraftforge.items.wrapper;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * An item handler seen as a vanilla container, for recipe matching. In 1.20.1 recipes matched against a
 * {@code Container}; in 26.3 against a {@link RecipeInput}, so this is both.
 */
public class RecipeWrapper implements Container, RecipeInput {
    protected final IItemHandlerModifiable inv;

    public RecipeWrapper(IItemHandlerModifiable inv) {
        this.inv = inv;
    }

    @Override
    public int getContainerSize() {
        return inv.getSlots();
    }

    @Override
    public int size() {
        return inv.getSlots();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = inv.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inv.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = getItem(slot);
        if (s.isEmpty()) return ItemStack.EMPTY;
        setItem(slot, ItemStack.EMPTY);
        return s;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inv.getSlots(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.isItemValid(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inv.getSlots(); i++) inv.setStackInSlot(i, ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}

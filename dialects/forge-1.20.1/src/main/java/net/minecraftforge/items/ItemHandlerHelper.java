package net.minecraftforge.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Forge's item handler helpers. Item stacks stack when their item and components match (26.x has no NBT tag). */
public class ItemHandlerHelper {
    /** Inserts into each slot in turn; returns what didn't fit. */
    public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty()) return stack;
        for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
    }

    /** Like canItemStacksStack, but ignores how full the stacks are; needs a stackable item. */
    public static boolean canItemStacksStackRelaxed(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.isStackable() && ItemStack.isSameItemSameComponents(a, b);
    }

    public static ItemStack copyStackWithSize(ItemStack stack, int size) {
        return size == 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
    }

    /** Tops up stacks of the same item first, then fills empty slots; returns what didn't fit. */
    public static ItemStack insertItemStacked(IItemHandler inventory, ItemStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty()) return stack;
        if (!stack.isStackable()) return insertItem(inventory, stack, simulate);
        int slots = inventory.getSlots();
        for (int i = 0; i < slots; i++) {
            if (canItemStacksStackRelaxed(inventory.getStackInSlot(i), stack)) {
                stack = inventory.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        for (int i = 0; i < slots; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                stack = inventory.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    public static void giveItemToPlayer(Player player, ItemStack stack) {
        giveItemToPlayer(player, stack, -1);
    }

    /**
     * Puts {@code stack} in the player's main inventory, trying {@code preferredSlot} first, and drops what doesn't
     * fit at the player's feet.
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;
        Inventory inventory = player.getInventory();
        Level level = player.level();
        int before = stack.getCount();
        ItemStack remainder = stack.copy();
        if (preferredSlot >= 0 && preferredSlot < inventory.getNonEquipmentItems().size()) {
            ItemStack inSlot = inventory.getItem(preferredSlot);
            if (inSlot.isEmpty() || canItemStacksStackRelaxed(inSlot, remainder)) inventory.add(preferredSlot, remainder);
        }
        if (!remainder.isEmpty()) inventory.add(remainder);
        if (remainder.getCount() != before) {
            level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        if (!remainder.isEmpty() && !level.isClientSide()) {
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            item.setPickUpDelay(40);
            item.setDeltaMovement(item.getDeltaMovement().multiply(0, 1, 0));
            level.addFreshEntity(item);
        }
    }

    /** A comparator signal from how full the handler is, as vanilla computes it for containers. */
    public static int calcRedstoneFromInventory(IItemHandler inv) {
        if (inv == null) return 0;
        int itemsFound = 0;
        float proportion = 0.0F;
        for (int j = 0; j < inv.getSlots(); j++) {
            ItemStack stack = inv.getStackInSlot(j);
            if (!stack.isEmpty()) {
                proportion += (float) stack.getCount() / (float) Math.min(inv.getSlotLimit(j), stack.getMaxStackSize());
                itemsFound++;
            }
        }
        proportion /= (float) inv.getSlots();
        return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
    }
}

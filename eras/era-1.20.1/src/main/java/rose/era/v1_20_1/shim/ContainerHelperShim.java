package rose.era.v1_20_1.shim;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Redirect targets for 1.20.1 {@code ContainerHelper} (26.3 reads and writes ValueInput/ValueOutput). Items keep
 * 1.20.1's layout, an {@code Items} list of stacks with a {@code Slot} byte, each stack as ItemNbtShim writes and reads
 * it (so 1.20.1 world data loads too).
 */
public final class ContainerHelperShim {
    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> items) {
        return saveAllItems(tag, items, true);
    }

    public static CompoundTag saveAllItems(CompoundTag tag, NonNullList<ItemStack> items, boolean alsoWhenEmpty) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) slot);
            list.add(ItemNbtShim.save(stack, entry));
        }
        if (!list.isEmpty() || alsoWhenEmpty) tag.put("Items", list);
        return tag;
    }

    public static void loadAllItems(CompoundTag tag, NonNullList<ItemStack> items) {
        for (Tag element : tag.getListOrEmpty("Items")) {
            if (!(element instanceof CompoundTag entry)) continue;
            int slot = entry.getByteOr("Slot", (byte) 0) & 255;
            if (slot < items.size()) items.set(slot, ItemNbtShim.of(entry));
        }
    }

    private ContainerHelperShim() {}
}

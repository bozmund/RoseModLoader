package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Era bridge (1.20.1): an item worn in an equipment slot. 1.21.2 replaced the interface with the {@code equippable}
 * component; an item implementing this gets that component with its slot and sound (LegacyItemComponents).
 */
public interface Equipable extends Vanishable {
    EquipmentSlot getEquipmentSlot();

    default SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC.value();
    }

    static Equipable get(ItemStack stack) {
        return stack.getItem() instanceof Equipable equipable ? equipable : null;
    }
}

package rose.dialect.forge.v1_20_1;

import net.minecraft.resources.Identifier;

/** Forge 1.20.1's slot background (Slot.setBackground), on every Slot (SlotForgeMixin). */
public interface ForgeSlot {
    Identifier rose$backgroundAtlas();

    Identifier rose$backgroundSprite();
}

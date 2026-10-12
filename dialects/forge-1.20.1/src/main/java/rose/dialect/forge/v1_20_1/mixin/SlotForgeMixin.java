package rose.dialect.forge.v1_20_1.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import rose.dialect.forge.v1_20_1.ForgeSlot;

/**
 * Forge 1.20.1's Slot additions (its Slot patch), which mods call on their own slots (Curios' CurioSlot):
 * {@code setBackground(atlas, sprite)}, the icon an empty slot shows, a sprite of a texture atlas (screens draw it,
 * SlotBackgroundScreenMixin; 26.3 slots name a GUI sprite instead); {@code getSlotIndex()}, the index in the slot's
 * container; {@code isSameInventory(other)}.
 */
@Mixin(Slot.class)
public abstract class SlotForgeMixin implements ForgeSlot {
    @Shadow
    @Final
    public Container container;

    @Unique
    private Identifier rose$atlas;

    @Unique
    private Identifier rose$sprite;

    @Shadow
    public abstract int getContainerSlot();

    public Slot setBackground(Identifier atlas, Identifier sprite) {
        rose$atlas = atlas;
        rose$sprite = sprite;
        return (Slot) (Object) this;
    }

    public int getSlotIndex() {
        return getContainerSlot();
    }

    public boolean isSameInventory(Slot other) {
        return container == other.container;
    }

    @Override
    public Identifier rose$backgroundAtlas() {
        return rose$atlas;
    }

    @Override
    public Identifier rose$backgroundSprite() {
        return rose$sprite;
    }
}

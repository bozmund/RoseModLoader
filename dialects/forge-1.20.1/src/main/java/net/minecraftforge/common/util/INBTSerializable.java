package net.minecraftforge.common.util;

import net.minecraft.nbt.Tag;

/** Something that saves itself to NBT and loads back from it. */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT();

    void deserializeNBT(T nbt);
}

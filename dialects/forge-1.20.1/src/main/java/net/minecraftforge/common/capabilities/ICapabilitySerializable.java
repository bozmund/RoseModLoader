package net.minecraftforge.common.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

/** A capability provider whose state is saved with the object it is attached to. */
public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider, INBTSerializable<T> {}

package net.minecraftforge.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

/** Something capabilities can be asked of (Forge block entities, entities and item stacks). */
public interface ICapabilityProvider {
    <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side);

    default <T> LazyOptional<T> getCapability(Capability<T> cap) {
        return getCapability(cap, null);
    }
}

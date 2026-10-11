package rose.dialect.forge.v1_20_1;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

/** Forge's capability methods on entities (CapabilityProvider): mixed into Entity, so mod calls on any entity type link. */
public interface CapabilityObject extends ICapabilityProvider {
    void reviveCaps();

    void invalidateCaps();
}

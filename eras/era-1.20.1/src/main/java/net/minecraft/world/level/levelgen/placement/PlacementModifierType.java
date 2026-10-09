package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;

/**
 * Era bridge (1.20.1): a placement modifier type was an object holding the modifier's codec. 26.3 registers the
 * MapCodec itself; RegistryAdapters unwraps registered types.
 */
public interface PlacementModifierType<P extends PlacementModifier> {
    Codec<P> codec();
}

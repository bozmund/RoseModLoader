package rose.era.v1_20_1.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

/**
 * Era bridge (1.20.1): fence gates took (Properties, WoodType), and Forge added (Properties, openSound, closeSound).
 * 26.3 takes (WoodType, Properties) and reads the gate sounds and sound type from the wood type, so custom sounds
 * become a private wood type that keeps the block's own sound type.
 */
public class LegacyFenceGateBlock extends FenceGateBlock {
    public LegacyFenceGateBlock(BlockBehaviour.Properties properties, WoodType type) {
        super(type, properties);
    }

    public LegacyFenceGateBlock(BlockBehaviour.Properties properties, SoundEvent openSound, SoundEvent closeSound) {
        super(soundsOnly(properties, openSound, closeSound), properties);
    }

    private static WoodType soundsOnly(BlockBehaviour.Properties properties, SoundEvent open, SoundEvent close) {
        WoodType oak = WoodType.OAK;
        return new WoodType("rose_custom_gate", oak.setType(), properties.soundType, oak.hangingSignSoundType(), close, open);
    }
}

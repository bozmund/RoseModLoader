package rose.era.v1_20_1.block;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import rose.era.v1_20_1.shim.Holders;

/**
 * Era bridge (1.20.1): FlowerBlock(MobEffect, int durationTicks, Properties). 26.3 takes the suspicious-stew effect
 * as a Holder and its duration in seconds.
 */
public class LegacyFlowerBlock extends FlowerBlock {
    public LegacyFlowerBlock(MobEffect suspiciousStewEffect, int effectDuration, BlockBehaviour.Properties properties) {
        super(Holders.mobEffect(suspiciousStewEffect), effectDuration / 20.0F, properties);
    }
}

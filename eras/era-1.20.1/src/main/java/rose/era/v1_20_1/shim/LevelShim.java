package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Redirect targets for 1.20.1 {@code Level} fields that 26.3 hides behind methods, and small world helpers. */
public final class LevelShim {
    /** 1.20.1 public field {@code level.isClientSide}; 26.3 made it private with {@code isClientSide()}. */
    public static boolean isClientSide(Level self) {
        return self.isClientSide();
    }

    /** 1.20.1 public field {@code level.random}; 26.3 made it protected with {@code getRandom()}. */
    public static RandomSource random(Level self) {
        return self.getRandom();
    }

    /** 1.20.1 {@code BlockPos.getCenter()}; 26.3 has {@code Vec3.atCenterOf(pos)}. */
    public static Vec3 getCenter(BlockPos pos) {
        return Vec3.atCenterOf(pos);
    }

    /** 1.20.1 {@code new ItemParticleOption(type, stack)}; 26.3 takes an ItemStackTemplate (or an Item). */
    public static ItemParticleOption itemParticle(ParticleType<ItemParticleOption> type, ItemStack stack) {
        return stack.isEmpty() ? new ItemParticleOption(type, stack.getItem()) : new ItemParticleOption(type, ItemStackTemplate.fromNonEmptyStack(stack));
    }

    private LevelShim() {}
}

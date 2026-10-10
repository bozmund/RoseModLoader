package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
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

    /**
     * 1.20.1 {@code Level.getGameRules()}; 26.x keeps game rules on the server only. A client level gets the default
     * rules, as a 1.20.1 client level held (game rules weren't synced to clients).
     */
    public static GameRules getGameRules(Level self) {
        if (self instanceof ServerLevel server) return server.getGameRules();
        GameRules defaults = clientDefaults;
        if (defaults == null) clientDefaults = defaults = new GameRules(self.enabledFeatures());
        return defaults;
    }

    private static volatile GameRules clientDefaults;

    /** 1.20.1 {@code GameRules.getBoolean(key)}; 26.3 {@code get(GameRule<Boolean>)} (the key became GameRule). */
    public static boolean getBoolean(GameRules self, GameRule<Boolean> rule) {
        return self.get(rule);
    }

    private LevelShim() {}
}

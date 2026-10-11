package rose.era.v1_20_1.shim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Redirect targets for 1.20.1 {@code Level} fields that 26.3 hides behind methods, and small world helpers. */
public final class LevelShim {
    /** 1.20.1 {@code level.getSharedSpawnPos()}: the world spawn, in 26.3 the position of {@code getRespawnData()}. */
    public static net.minecraft.core.BlockPos getSharedSpawnPos(Level self) {
        return self.getRespawnData().pos();
    }

    /** 1.20.1 {@code level.getSharedSpawnAngle()}: the spawn yaw, in 26.3 {@code getRespawnData().yaw()}. */
    public static float getSharedSpawnAngle(Level self) {
        return self.getRespawnData().yaw();
    }

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

    /** 1.20.1 {@code GameRules.getInt(key)}; 26.3 {@code get(GameRule<Integer>)}. */
    public static int getInt(GameRules self, GameRule<Integer> rule) {
        return self.get(rule);
    }

    /**
     * 1.20.1 {@code Level.getDayTime()}: the overworld's time of day, which every dimension shared. 26.x keeps time in
     * world clocks; the overworld clock is the one that time of day followed.
     */
    public static long getDayTime(Level self) {
        return self.getOverworldClockTime();
    }

    // 1.20.1 explode(...) returned the Explosion it ran; 26.3 runs it the same way but returns nothing.

    public static Explosion explode(Level self, @Nullable Entity source, double x, double y, double z, float radius,
            Level.ExplosionInteraction interaction) {
        self.explode(source, x, y, z, radius, interaction);
        return null;
    }

    public static Explosion explode(Level self, @Nullable Entity source, double x, double y, double z, float radius, boolean fire,
            Level.ExplosionInteraction interaction) {
        self.explode(source, x, y, z, radius, fire, interaction);
        return null;
    }

    public static Explosion explode(Level self, @Nullable Entity source, @Nullable DamageSource damage,
            @Nullable ExplosionDamageCalculator calculator, Vec3 pos, float radius, boolean fire, Level.ExplosionInteraction interaction) {
        self.explode(source, damage, calculator, pos, radius, fire, interaction);
        return null;
    }

    public static Explosion explode(Level self, @Nullable Entity source, @Nullable DamageSource damage,
            @Nullable ExplosionDamageCalculator calculator, double x, double y, double z, float radius, boolean fire,
            Level.ExplosionInteraction interaction) {
        self.explode(source, damage, calculator, new Vec3(x, y, z), radius, fire, interaction);
        return null;
    }

    /** The 1.20.1 overload with a particles flag; 26.3 explosions always send their particles. */
    public static Explosion explode(Level self, @Nullable Entity source, @Nullable DamageSource damage,
            @Nullable ExplosionDamageCalculator calculator, double x, double y, double z, float radius, boolean fire,
            Level.ExplosionInteraction interaction, boolean particles) {
        return explode(self, source, damage, calculator, x, y, z, radius, fire, interaction);
    }

    private LevelShim() {}
}

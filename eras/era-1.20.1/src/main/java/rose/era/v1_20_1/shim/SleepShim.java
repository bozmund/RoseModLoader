package rose.era.v1_20_1.shim;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Redirect targets for 1.20.1 sleeping. 1.20.1 {@code Player.BedSleepingProblem} was an enum; 26.3 made it a record
 * holding the message, and replaced the dimension's {@code natural}/{@code bedWorks} flags with the {@code bed_rule}
 * environment attribute.
 */
public final class SleepShim {
    /** 1.20.1 NOT_POSSIBLE_HERE (no message: the bed explodes instead). */
    private static final Player.BedSleepingProblem NOT_POSSIBLE_HERE = new Player.BedSleepingProblem(null);
    /** 1.20.1 NOT_POSSIBLE_NOW; equal to what 26.3's default bed rule reports in daylight. */
    private static final Player.BedSleepingProblem NOT_POSSIBLE_NOW =
            new Player.BedSleepingProblem(Component.translatable("block.minecraft.bed.no_sleep"));
    /** The 1.20.1 enum constants in declaration order, so ordinals match. */
    private static final Player.BedSleepingProblem[] VALUES = {
            NOT_POSSIBLE_HERE, NOT_POSSIBLE_NOW, Player.BedSleepingProblem.TOO_FAR_AWAY, Player.BedSleepingProblem.OBSTRUCTED,
            Player.BedSleepingProblem.OTHER_PROBLEM, Player.BedSleepingProblem.NOT_SAFE};
    private static final int OTHER_PROBLEM = 4;

    public static Player.BedSleepingProblem NOT_POSSIBLE_HERE() {
        return NOT_POSSIBLE_HERE;
    }

    public static Player.BedSleepingProblem NOT_POSSIBLE_NOW() {
        return NOT_POSSIBLE_NOW;
    }

    /** 1.20.1 {@code BedSleepingProblem.values()}. */
    public static Player.BedSleepingProblem[] values() {
        return VALUES.clone();
    }

    /**
     * 1.20.1 {@code problem.ordinal()} (switches use it). A 26.3 problem made elsewhere is matched by its message: the
     * default bed rule's daylight problem is NOT_POSSIBLE_NOW; one without a message is OTHER_PROBLEM.
     */
    public static int ordinal(Player.BedSleepingProblem problem) {
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i] == problem) return i;
        }
        for (int i = 1; i < VALUES.length; i++) {
            if (VALUES[i].equals(problem)) return i;
        }
        return OTHER_PROBLEM;
    }

    /** 1.20.1 {@code problem.getMessage()}. */
    public static Component getMessage(Player.BedSleepingProblem problem) {
        return problem.message();
    }

    /** 1.20.1 {@code DimensionType.natural()}: sleeping works there (26.3: the dimension's bed rule ever allows it). */
    public static boolean natural(DimensionType type) {
        return bedRule(type).canSleep() != BedRule.Rule.NEVER;
    }

    /** 1.20.1 {@code BedBlock.canSetSpawn(level)}, i.e. the dimension's {@code bedWorks}: beds don't explode there. */
    public static boolean canSetSpawn(Level level) {
        return !bedRule(level.dimensionType()).destroyOnUse();
    }

    /** 1.20.1 {@code Monster.isPreventingPlayerRest(player)}; 26.3 also takes the level, which only a server has. */
    public static boolean isPreventingPlayerRest(Monster monster, Player player) {
        return player.level() instanceof ServerLevel level && monster.isPreventingPlayerRest(level, player);
    }

    private static BedRule bedRule(DimensionType type) {
        return type.attributes().applyModifier(EnvironmentAttributes.BED_RULE, EnvironmentAttributes.BED_RULE.defaultValue());
    }

    private SleepShim() {}
}

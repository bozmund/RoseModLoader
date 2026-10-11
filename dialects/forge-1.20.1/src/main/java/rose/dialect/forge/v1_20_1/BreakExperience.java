package rose.dialect.forge.v1_20_1;

/**
 * Forge 1.20.1 dropped the experience a BreakEvent ended with instead of the block's own. Rose leaves 26.3's drops
 * alone unless a listener changed the amount; then, while that block breaks, the block's own experience is held back
 * (BlockExperienceMixin) and the event's amount drops afterwards.
 */
public final class BreakExperience {
    private static final ThreadLocal<int[]> OVERRIDE = new ThreadLocal<>();

    public static void override(int experience) {
        OVERRIDE.set(new int[] {experience});
    }

    /** Whether the block breaking on this thread has its experience replaced. */
    public static boolean overridden() {
        return OVERRIDE.get() != null;
    }

    /** The replacement amount, or -1 if none; clears it. */
    public static int take() {
        int[] value = OVERRIDE.get();
        OVERRIDE.remove();
        return value == null ? -1 : value[0];
    }

    private BreakExperience() {}
}

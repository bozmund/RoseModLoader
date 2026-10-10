package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;

/** Bridge helpers for containers (rules/forge-1.20.1/bridges.tsv). */
public final class ContainerBridges {
    private static final MethodType OPEN = MethodType.methodType(void.class, Player.class);

    /** 1.20.1 {@code startOpen(Player)}; 26.x opens for any ContainerUser (only players opened containers in 1.20.1). */
    public static void startOpen(Container self, ContainerUser user) {
        if (user instanceof Player player) Legacy.invoke(Legacy.require(self, "startOpen", OPEN), self, player);
    }

    /** 1.20.1 {@code stopOpen(Player)}; see {@link #startOpen}. */
    public static void stopOpen(Container self, ContainerUser user) {
        if (user instanceof Player player) Legacy.invoke(Legacy.require(self, "stopOpen", OPEN), self, player);
    }

    private ContainerBridges() {}
}

package rose.dialect.forge.v1_20_1;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;

/** Forge 1.20.1's sleep hooks (ForgeEventFactory.onPlayerSleepInBed / fireSleepingTimeCheck). */
public final class SleepEvents {
    /** The problem a listener set, or {@code null} to let the player try. */
    public static Player.BedSleepingProblem sleepInBed(Player player, Optional<BlockPos> pos) {
        if (!ForgeDialect.active()) return null;
        PlayerSleepInBedEvent event = new PlayerSleepInBedEvent(player, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResultStatus();
    }

    /** Whether the player may sleep now: a listener's ALLOW or DENY, else {@code vanilla} (the bed rule's answer). */
    public static boolean sleepingTime(Player player, Optional<BlockPos> pos, boolean vanilla) {
        if (!ForgeDialect.active()) return vanilla;
        SleepingTimeCheckEvent event = new SleepingTimeCheckEvent(player, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.DEFAULT ? vanilla : event.getResult() == Event.Result.ALLOW;
    }

    private SleepEvents() {}
}

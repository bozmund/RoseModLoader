package net.minecraftforge.event.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/** A player tries to sleep; setting a problem stops it with that problem. */
public class PlayerSleepInBedEvent extends PlayerEvent {
    private Player.BedSleepingProblem result;
    private final Optional<BlockPos> pos;

    public PlayerSleepInBedEvent(Player player, Optional<BlockPos> pos) {
        super(player);
        this.pos = pos;
    }

    public Player.BedSleepingProblem getResultStatus() {
        return result;
    }

    public void setResult(Player.BedSleepingProblem result) {
        this.result = result;
    }

    public BlockPos getPos() {
        return pos.orElse(null);
    }

    public Optional<BlockPos> getOptionalPos() {
        return pos;
    }
}

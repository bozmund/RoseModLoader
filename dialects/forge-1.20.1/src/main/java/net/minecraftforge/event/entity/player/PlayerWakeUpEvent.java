package net.minecraftforge.event.entity.player;

import net.minecraft.world.entity.player.Player;

/** A player is getting out of bed. */
public class PlayerWakeUpEvent extends PlayerEvent {
    private final boolean wakeImmediately;
    private final boolean updateLevel;

    public PlayerWakeUpEvent(Player player, boolean wakeImmediately, boolean updateLevel) {
        super(player);
        this.wakeImmediately = wakeImmediately;
        this.updateLevel = updateLevel;
    }

    public boolean wakeImmediately() {
        return wakeImmediately;
    }

    public boolean updateLevel() {
        return updateLevel;
    }
}

package net.minecraftforge.event.entity.living;

import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;

/** A player looking at an enderman is about to anger it; canceling lets the player look. */
@Cancelable
public class EnderManAngerEvent extends LivingEvent {
    private final Player player;

    public EnderManAngerEvent(Enderman enderman, Player player) {
        super(enderman);
        this.player = player;
    }

    @Override
    public Enderman getEntity() {
        return (Enderman) super.getEntity();
    }

    public Player getPlayer() {
        return player;
    }
}

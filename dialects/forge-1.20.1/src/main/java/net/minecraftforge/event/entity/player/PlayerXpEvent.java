package net.minecraftforge.event.entity.player;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;

/** Player experience events. */
public class PlayerXpEvent extends PlayerEvent {
    public PlayerXpEvent(Player player) {
        super(player);
    }

    /** A player is about to pick up an experience orb; canceling leaves the orb. */
    @Cancelable
    public static class PickupXp extends PlayerXpEvent {
        private final ExperienceOrb orb;

        public PickupXp(Player player, ExperienceOrb orb) {
            super(player);
            this.orb = orb;
        }

        public ExperienceOrb getOrb() {
            return orb;
        }
    }
}

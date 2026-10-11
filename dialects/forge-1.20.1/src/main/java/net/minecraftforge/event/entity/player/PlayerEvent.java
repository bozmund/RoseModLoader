package net.minecraftforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PlayerEvent extends LivingEvent {
    private final Player player;

    public PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    /**
     * A player was re-created (respawn, or return from the End): {@code getEntity()} is the new player. Mods copy
     * their data from the original, whose capabilities are already invalidated (call reviveCaps to read them).
     */
    public static class Clone extends PlayerEvent {
        private final Player original;
        private final boolean wasDeath;

        public Clone(Player player, Player original, boolean wasDeath) {
            super(player);
            this.original = original;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() {
            return original;
        }

        public boolean isWasDeath() {
            return wasDeath;
        }
    }
}

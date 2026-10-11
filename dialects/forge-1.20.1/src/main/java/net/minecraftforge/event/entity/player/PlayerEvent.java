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

    /** A player joined the server (after it was placed in the world). */
    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }

    /** A player is leaving the server (before it is removed). */
    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    /** A player respawned (the new player); {@code endConquered} when returning from the End. */
    public static class PlayerRespawnEvent extends PlayerEvent {
        private final boolean endConquered;

        public PlayerRespawnEvent(Player player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        public boolean isEndConquered() {
            return endConquered;
        }
    }

    /** A player moved to another dimension. */
    public static class PlayerChangedDimensionEvent extends PlayerEvent {
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to;

        public PlayerChangedDimensionEvent(Player player, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> from,
                                           net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> to) {
            super(player);
            this.from = from;
            this.to = to;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getFrom() {
            return from;
        }

        public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getTo() {
            return to;
        }
    }

    /** A player's client starts receiving an entity (its pairing was added on the server). */
    public static class StartTracking extends PlayerEvent {
        private final net.minecraft.world.entity.Entity target;

        public StartTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return target;
        }
    }

    /** A player's client stops receiving an entity. */
    public static class StopTracking extends PlayerEvent {
        private final net.minecraft.world.entity.Entity target;

        public StopTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return target;
        }
    }
}

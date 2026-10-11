package net.minecraftforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;

/** A player's respawn point is about to be set; canceling keeps the old one. */
@Cancelable
public class PlayerSetSpawnEvent extends PlayerEvent {
    private final ResourceKey<Level> spawnLevel;
    private final BlockPos newSpawn;
    private final boolean forced;

    public PlayerSetSpawnEvent(Player player, ResourceKey<Level> spawnLevel, BlockPos newSpawn, boolean forced) {
        super(player);
        this.spawnLevel = spawnLevel;
        this.newSpawn = newSpawn;
        this.forced = forced;
    }

    public ResourceKey<Level> getSpawnLevel() {
        return spawnLevel;
    }

    public BlockPos getNewSpawn() {
        return newSpawn;
    }

    public boolean isForced() {
        return forced;
    }
}

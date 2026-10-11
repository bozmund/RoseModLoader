package net.minecraftforge.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.eventbus.api.Event;

/** Data is being sent to players: one joining ({@code getPlayer()}), or everyone after /reload (player {@code null}). */
public class OnDatapackSyncEvent extends Event {
    private final PlayerList playerList;
    private final ServerPlayer player;

    public OnDatapackSyncEvent(PlayerList playerList, ServerPlayer player) {
        this.playerList = playerList;
        this.player = player;
    }

    public PlayerList getPlayerList() {
        return playerList;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}

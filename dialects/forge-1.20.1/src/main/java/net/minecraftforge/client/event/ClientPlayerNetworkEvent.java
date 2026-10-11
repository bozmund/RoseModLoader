package net.minecraftforge.client.event;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraftforge.eventbus.api.Event;
import org.jspecify.annotations.Nullable;

/** The client's player joined or left a server (or a singleplayer world). */
public abstract class ClientPlayerNetworkEvent extends Event {
    private final @Nullable MultiPlayerGameMode multiPlayerGameMode;
    private final @Nullable LocalPlayer player;
    private final @Nullable Connection connection;

    protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode multiPlayerGameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
        this.multiPlayerGameMode = multiPlayerGameMode;
        this.player = player;
        this.connection = connection;
    }

    public @Nullable MultiPlayerGameMode getMultiPlayerGameMode() {
        return multiPlayerGameMode;
    }

    public @Nullable LocalPlayer getPlayer() {
        return player;
    }

    public @Nullable Connection getConnection() {
        return connection;
    }

    public static class LoggingIn extends ClientPlayerNetworkEvent {
        public LoggingIn(MultiPlayerGameMode gameMode, LocalPlayer player, Connection connection) {
            super(gameMode, player, connection);
        }
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {
        public LoggingOut(@Nullable MultiPlayerGameMode gameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
            super(gameMode, player, connection);
        }
    }
}

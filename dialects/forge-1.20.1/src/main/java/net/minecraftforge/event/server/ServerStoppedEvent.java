package net.minecraftforge.event.server;

import net.minecraft.server.MinecraftServer;

/** The server has stopped: worlds are saved and closed. */
public class ServerStoppedEvent extends ServerLifecycleEvent {
    public ServerStoppedEvent(MinecraftServer server) {
        super(server);
    }
}

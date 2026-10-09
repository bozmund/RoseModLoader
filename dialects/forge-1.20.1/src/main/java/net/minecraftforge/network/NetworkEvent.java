package net.minecraftforge.network;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class NetworkEvent extends Event {
    /** Who sent a message and on which side it is handled. Rose delivers messages on the game thread. */
    public static class Context {
        private final NetworkDirection direction;
        private final ServerPlayer sender;
        private boolean packetHandled;

        public Context(NetworkDirection direction, ServerPlayer sender) {
            this.direction = direction;
            this.sender = sender;
        }

        public NetworkDirection getDirection() {
            return direction;
        }

        /** The player who sent this message to the server; {@code null} on the client. */
        public ServerPlayer getSender() {
            return sender;
        }

        public void setPacketHandled(boolean packetHandled) {
            this.packetHandled = packetHandled;
        }

        public boolean getPacketHandled() {
            return packetHandled;
        }

        public CompletableFuture<Void> enqueueWork(Runnable runnable) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}

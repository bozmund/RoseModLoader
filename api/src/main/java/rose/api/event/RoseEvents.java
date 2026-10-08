package rose.api.event;

import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Common (both-sides) events fired by Rose core. */
public final class RoseEvents {
    /** The server finished starting and is about to run its first tick. */
    public static final Event<Consumer<MinecraftServer>> SERVER_STARTED = Event.ofConsumer();

    /** The server is about to shut down (worlds are still loaded). */
    public static final Event<Consumer<MinecraftServer>> SERVER_STOPPING = Event.ofConsumer();

    /** End of every server tick. */
    public static final Event<Consumer<MinecraftServer>> SERVER_TICK_END = Event.ofConsumer();

    /** A player finished joining the server and is in the world. */
    public static final Event<Consumer<ServerPlayer>> PLAYER_JOIN = Event.ofConsumer();

    private RoseEvents() {}
}

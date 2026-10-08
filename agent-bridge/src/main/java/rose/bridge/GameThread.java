package rose.bridge;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

/** Bridge calls arrive on HTTP threads; game state may only be touched on the game's own threads. */
public final class GameThread {
    private static final long TIMEOUT_SECONDS = 30;

    public static <T> T onServer(MinecraftServer server, Supplier<T> task) throws Exception {
        if (server.isSameThread()) return task.get();
        return server.submit(task).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private GameThread() {}
}

package net.minecraftforge.fml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Messages between mods during loading: sent while enqueueing, read (once) while processing. */
public final class InterModComms {
    public record IMCMessage(String senderModId, String modId, String method, Supplier<?> thing) {
        @SuppressWarnings("unchecked")
        public <T> Supplier<T> messageSupplier() {
            return (Supplier<T>) thing;
        }

        public String getSenderModId() {
            return senderModId;
        }

        public String getModId() {
            return modId;
        }

        public String getMethod() {
            return method;
        }
    }

    private static final Map<String, List<IMCMessage>> QUEUES = new ConcurrentHashMap<>();

    public static boolean sendTo(String modId, String method, Supplier<?> thing) {
        return sendTo(ModLoadingContext.get().getActiveNamespace(), modId, method, thing);
    }

    public static boolean sendTo(String senderModId, String modId, String method, Supplier<?> thing) {
        if (!ModList.get().isLoaded(modId)) return false;
        QUEUES.computeIfAbsent(modId, k -> new ArrayList<>()).add(new IMCMessage(senderModId, modId, method, thing));
        return true;
    }

    /** The messages sent to {@code modId}; reading them removes them, as Forge's queue did. */
    public static Stream<IMCMessage> getMessages(String modId) {
        return getMessages(modId, method -> true);
    }

    public static Stream<IMCMessage> getMessages(String modId, Predicate<String> methodMatcher) {
        List<IMCMessage> queue = QUEUES.getOrDefault(modId, new ArrayList<>());
        List<IMCMessage> taken = new ArrayList<>();
        synchronized (queue) {
            queue.removeIf(m -> methodMatcher.test(m.method()) && taken.add(m));
        }
        return taken.stream();
    }

    private InterModComms() {}
}

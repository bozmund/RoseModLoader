package rose.dialect.forge.v1_20_1;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/**
 * Records Forge features a mod used that the dialect doesn't provide yet. Each one is logged once (as a warning,
 * so the mod keeps loading) and collected, so the foundry and {@code rose analyze} users can see what to build next.
 */
public final class Unsupported {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();
    private static final Map<String, String> ALL = new ConcurrentHashMap<>();

    public static void event(String eventClass, String listener) {
        report("event:" + eventClass, "listener " + listener + " skipped: Rose doesn't fire " + eventClass + " yet");
    }

    public static void listener(String listener, Throwable cause) {
        report("listener:" + listener, "listener " + listener + " skipped: " + cause);
    }

    public static void untypedListener(String lambdaClass) {
        report("untyped:" + lambdaClass, "listener " + lambdaClass + " has no known event type; it receives all events");
    }

    public static void registry(String registry, String detail) {
        report("registry:" + registry, detail);
    }

    public static void feature(String key, String detail) {
        report(key, detail);
    }

    private static void report(String key, String message) {
        ALL.putIfAbsent(key, message);
        if (SEEN.add(key)) LOG.warn("[rose/forge] {}", message);
    }

    /** Everything reported so far, by key. */
    public static Map<String, String> all() {
        return new TreeMap<>(ALL);
    }

    private Unsupported() {}
}

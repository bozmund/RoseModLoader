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
    /** {@code -Drose.forge.strict=true}: fail on the first entry or listener that can't be loaded. */
    public static final boolean STRICT = Boolean.getBoolean("rose.forge.strict");
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

    /** A registry entry whose creation failed; the cause chain's innermost message says what's missing. */
    public static void entry(String id, String registry, Throwable cause) {
        Throwable root = cause;
        while (root.getCause() != null) root = root.getCause();
        String key = "entry:" + id;
        ALL.putIfAbsent(key, registry + " entry " + id + " skipped: " + root);
        if (SEEN.add(key)) LOG.warn("[rose/forge] {} entry {} skipped: {}", registry, id, root.toString(), cause);
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

package rose.testmods.hello;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Called from the test mod's mixins. Each marker line is printed once so tests and agents can grep for it. */
public final class HelloRose {
    private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

    public static void mark(String where) {
        if (SEEN.add(where)) System.out.println("[hello] mixin applied: " + where);
    }

    private HelloRose() {}
}

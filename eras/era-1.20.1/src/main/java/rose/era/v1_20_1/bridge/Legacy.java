package rose.era.v1_20_1.bridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds a mod object's old (1.20.1-shaped) method for bridge helpers, by exact name and type, so a helper never
 * finds the bridge that called it. Results are cached per class.
 */
public final class Legacy {
    private record Key(Class<?> type, String name, MethodType methodType) {}

    private static final Map<Key, Optional<MethodHandle>> CACHE = new ConcurrentHashMap<>();
    private static final Map<Key, MethodHandle> SUPER_CACHE = new ConcurrentHashMap<>();

    /** {@code self}'s virtual method {@code name} of the given type, if its class has one. */
    public static Optional<MethodHandle> find(Object self, String name, MethodType type) {
        return CACHE.computeIfAbsent(new Key(self.getClass(), name, type), k -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(k.type(), MethodHandles.lookup());
                return Optional.of(lookup.findVirtual(k.type(), name, type));
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Can't access " + k.type().getName() + "." + name + type, e);
            }
        });
    }

    public static MethodHandle require(Object self, String name, MethodType type) {
        return find(self, name, type).orElseThrow(() -> new IllegalStateException(
                self.getClass().getName() + " has no " + name + type + " to bridge to"));
    }

    /**
     * The game's own (26.3) method {@code name} for a mod object, called non-virtually: the implementation in the
     * nearest superclass that isn't mod code. A mod's {@code super.method(...)} with a 1.20.1 signature lands here;
     * a virtual call would reach the mod's bridge and loop back into the mod.
     */
    public static MethodHandle superMethod(Object self, String name, MethodType type) {
        return SUPER_CACHE.computeIfAbsent(new Key(self.getClass(), name, type), k -> {
            Class<?> game = k.type();
            while (!isGameClass(game)) game = game.getSuperclass();
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(game, MethodHandles.lookup());
                return lookup.findSpecial(game, name, type, game);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No " + name + type + " in " + game.getName(), e);
            }
        });
    }

    private static boolean isGameClass(Class<?> type) {
        String name = type.getName();
        return name.startsWith("net.minecraft.") || name.startsWith("rose.") || type == Object.class;
    }

    /** Calls a handle, rethrowing unchecked exceptions unchanged. */
    public static Object invoke(MethodHandle handle, Object... args) {
        try {
            return handle.invokeWithArguments(args);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    private Legacy() {}
}

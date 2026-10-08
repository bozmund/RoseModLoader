package rose.loader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The running loader's state: which side this is, which mods loaded, and their entrypoints. Set once by the
 * launcher. Rose core (which runs inside {@link RoseClassLoader}) reads it, since this class is shared from the
 * parent loader.
 */
public final class RoseLoader {
    public enum Side { CLIENT, SERVER }

    private static volatile RoseLoader instance;

    private final Side side;
    private final List<ModMetadata> mods;
    private final RoseClassLoader classLoader;

    private RoseLoader(Side side, List<ModMetadata> mods, RoseClassLoader classLoader) {
        this.side = side;
        this.mods = List.copyOf(mods);
        this.classLoader = classLoader;
    }

    public static synchronized void initialize(Side side, List<ModMetadata> mods, RoseClassLoader classLoader) {
        if (instance != null) throw new IllegalStateException("RoseLoader is already initialized");
        instance = new RoseLoader(side, mods, classLoader);
    }

    public static RoseLoader get() {
        RoseLoader loader = instance;
        if (loader == null) throw new IllegalStateException("RoseLoader is not initialized");
        return loader;
    }

    public Side side() { return side; }

    public List<ModMetadata> mods() { return mods; }

    public Optional<ModMetadata> mod(String id) {
        return mods.stream().filter(m -> m.id().equals(id)).findFirst();
    }

    public RoseClassLoader classLoader() { return classLoader; }

    /**
     * Instantiates every mod's entrypoints of one kind, in mod order. Each class needs a public no-arg constructor
     * and must implement {@code type}.
     */
    public <T> List<Entrypoint<T>> entrypoints(String kind, Class<T> type) {
        List<Entrypoint<T>> out = new ArrayList<>();
        for (ModMetadata mod : mods) {
            for (String className : mod.entrypoints(kind)) {
                try {
                    Class<?> cls = Class.forName(className, true, classLoader);
                    if (!type.isAssignableFrom(cls)) {
                        throw new IllegalStateException(className + " must implement " + type.getName());
                    }
                    Constructor<?> ctor = cls.getDeclaredConstructor();
                    out.add(new Entrypoint<>(mod, type.cast(ctor.newInstance())));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Mod '" + mod.id() + "': could not create " + kind
                            + " entrypoint " + className, e);
                }
            }
        }
        return out;
    }

    public record Entrypoint<T>(ModMetadata mod, T instance) {}
}

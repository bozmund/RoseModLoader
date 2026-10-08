package rose.mixin;

import java.lang.reflect.Method;
import java.util.Collection;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import rose.loader.RoseClassLoader;

/**
 * Starts Mixin for one game launch and installs it as a {@link rose.loader.ClassTransformer} on Rose's loader.
 * Mixin is global per JVM, so this runs once.
 */
public final class RoseMixin {
    public static final String CLIENT = "CLIENT";
    public static final String SERVER = "SERVER";

    private static volatile RoseClassLoader loader;
    private static volatile String side = "UNKNOWN";
    private static volatile IMixinTransformerFactory transformerFactory;

    /**
     * @param side          {@link #CLIENT} or {@link #SERVER}; decides whether a config's "client" or "server"
     *                      mixin lists apply
     * @param mixinConfigs  resource names of mixin config files (e.g. {@code hello.mixins.json}) on the loader
     */
    public static synchronized void bootstrap(RoseClassLoader roseLoader, String side, Collection<String> mixinConfigs) {
        if (loader != null) throw new IllegalStateException("Mixin is already bootstrapped");
        loader = roseLoader;
        RoseMixin.side = side;

        MixinBootstrap.init();
        for (String config : mixinConfigs) Mixins.addConfiguration(config);
        enterDefaultPhase();

        if (transformerFactory == null) throw new IllegalStateException("Mixin did not offer a transformer factory");
        IMixinTransformer transformer = transformerFactory.createTransformer();
        // MixinExtras registers itself as an extension of the active transformer, so it must start after it.
        MixinExtrasBootstrap.init();
        roseLoader.setFinalTransformer((internalName, bytes) -> {
            String name = internalName.replace('/', '.');
            return transformer.transformClassBytes(name, name, bytes);
        });
    }

    /**
     * Mixin only selects and applies configs once the environment reaches the DEFAULT phase. Launchers move it
     * there through a non-public method (Fabric Loader does the same).
     */
    private static void enterDefaultPhase() {
        try {
            Method gotoPhase = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            gotoPhase.setAccessible(true);
            gotoPhase.invoke(null, MixinEnvironment.Phase.INIT);
            gotoPhase.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not move Mixin to the DEFAULT phase", e);
        }
    }

    static RoseClassLoader loader() { return loader; }

    static String side() { return side; }

    static void setTransformerFactory(IMixinTransformerFactory factory) { transformerFactory = factory; }

    private RoseMixin() {}
}

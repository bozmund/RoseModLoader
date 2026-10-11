package net.minecraftforge.fml;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Runs code only on one side. The double supplier keeps client-only classes unloaded on a dedicated server: the
 * outer supplier is only called on the matching side. Forge's "safe" variants also checked the referent was a method
 * reference; Rose runs them the same way as the unsafe ones.
 *
 * <p>Forge constructed mods inside Minecraft's constructor; Rose constructs them before the client exists (registries
 * freeze first on 26.3). Client runnables from that time (GeckoLib registering its reload listener) wait for
 * {@link #clientCreated()}, when Minecraft and its resource manager exist and before resources first load.
 */
public final class DistExecutor {
    private DistExecutor() {}

    private static final java.util.List<Runnable> waitingForClient = new java.util.ArrayList<>();
    private static boolean clientCreated;

    /** Rose: the client exists now; runs the client runnables mods queued while being constructed. */
    public static synchronized void clientCreated() {
        clientCreated = true;
        for (Runnable r : waitingForClient) r.run();
        waitingForClient.clear();
    }

    private static synchronized boolean waitForClient(Dist dist, Runnable run) {
        if (dist != Dist.CLIENT || FMLEnvironment.dist != Dist.CLIENT || clientCreated) return false;
        var mod = rose.dialect.forge.v1_20_1.ForgeDialect.activeMod();
        waitingForClient.add(mod == null ? run : () -> rose.dialect.forge.v1_20_1.ForgeDialect.runAs(mod, run));
        return true;
    }

    @Deprecated
    public static <T> T callWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
        return unsafeCallWhenOn(dist, toRun);
    }

    public static <T> T unsafeCallWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
        if (dist != FMLEnvironment.dist) return null;
        try {
            return toRun.get().call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T safeCallWhenOn(Dist dist, Supplier<SafeCallable<T>> toRun) {
        if (dist != FMLEnvironment.dist) return null;
        try {
            return toRun.get().call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public static void runWhenOn(Dist dist, Supplier<Runnable> toRun) {
        unsafeRunWhenOn(dist, toRun);
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        if (dist == FMLEnvironment.dist && !waitForClient(dist, () -> toRun.get().run())) toRun.get().run();
    }

    public static void safeRunWhenOn(Dist dist, Supplier<SafeRunnable> toRun) {
        if (dist == FMLEnvironment.dist && !waitForClient(dist, () -> toRun.get().run())) toRun.get().run();
    }

    @Deprecated
    public static <T> T runForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return unsafeRunForDist(clientTarget, serverTarget);
    }

    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return (FMLEnvironment.dist == Dist.CLIENT ? clientTarget : serverTarget).get().get();
    }

    public static <T> T safeRunForDist(Supplier<SafeSupplier<T>> clientTarget, Supplier<SafeSupplier<T>> serverTarget) {
        return (FMLEnvironment.dist == Dist.CLIENT ? clientTarget : serverTarget).get().get();
    }

    public interface SafeReferent {}

    public interface SafeCallable<T> extends SafeReferent, Callable<T>, Serializable {}

    public interface SafeSupplier<T> extends SafeReferent, Supplier<T>, Serializable {}

    public interface SafeRunnable extends SafeReferent, Runnable, Serializable {}
}

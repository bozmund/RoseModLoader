package rose.era.v1_20_1.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * A 1.20.1 reload listener: {@code reload(barrier, manager, prepareProfiler, applyProfiler, backgroundExecutor,
 * gameExecutor)}. 26.x passes a shared state (holding the resource manager) and dropped the profilers.
 */
@FunctionalInterface
public interface LegacyPreparableReloadListener extends PreparableReloadListener {
    CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                                   ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor);

    @Override
    default CompletableFuture<Void> reload(SharedState state, Executor taskExecutor, PreparationBarrier barrier, Executor reloadExecutor) {
        return reload(barrier, state.resourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, taskExecutor, reloadExecutor);
    }
}

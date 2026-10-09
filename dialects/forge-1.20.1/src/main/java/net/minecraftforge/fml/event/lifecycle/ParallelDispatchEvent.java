package net.minecraftforge.fml.event.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * A lifecycle event. Forge dispatched these to mods in parallel and ran {@link #enqueueWork} jobs afterwards on the
 * main thread; Rose dispatches on the main thread and runs the queued jobs right after every mod got the event.
 */
public abstract class ParallelDispatchEvent extends Event implements IModBusEvent {
    private final List<Runnable> work = new ArrayList<>();

    public CompletableFuture<Void> enqueueWork(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        work.add(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
                throw t;
            }
        });
        return future;
    }

    public <T> CompletableFuture<T> enqueueWork(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        work.add(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
                throw t;
            }
        });
        return future;
    }

    /** The jobs queued so far (Rose runs them). */
    public List<Runnable> rose$drainWork() {
        List<Runnable> out = List.copyOf(work);
        work.clear();
        return out;
    }
}

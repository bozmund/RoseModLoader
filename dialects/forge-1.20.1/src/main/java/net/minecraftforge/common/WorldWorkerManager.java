package net.minecraftforge.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 1.20.1's world workers: long jobs (Nature's Compass' biome search, world pregenerators) done a slice at a time
 * at the end of each server tick. Rose gives them Forge's minimum slice, 10 ms a tick.
 */
public final class WorldWorkerManager {
    private static final long SLICE_NANOS = 10_000_000L;
    private static final List<IWorker> workers = new ArrayList<>();
    private static int index;

    /** Runs workers until this tick's slice is used up. */
    public static void tick(boolean start) {
        if (start) return;
        long end = System.nanoTime() + SLICE_NANOS;
        while (System.nanoTime() < end) {
            IWorker task = next();
            if (task == null) return;
            boolean again = task.doWork();
            if (!task.hasWork()) remove(task);
            else if (!again) return;
        }
    }

    public static synchronized void addWorker(IWorker worker) {
        workers.add(worker);
    }

    private static synchronized IWorker next() {
        if (workers.isEmpty()) return null;
        if (index >= workers.size()) index = 0;
        return workers.get(index++);
    }

    private static synchronized void remove(IWorker worker) {
        workers.remove(worker);
        index = 0;
    }

    /** Drops every worker (the server stopped). */
    public static synchronized void clear() {
        workers.clear();
        index = 0;
    }

    private WorldWorkerManager() {}

    public interface IWorker {
        boolean hasWork();

        /** Does a piece of the work; {@code false} to wait for the next tick. */
        boolean doWork();
    }
}

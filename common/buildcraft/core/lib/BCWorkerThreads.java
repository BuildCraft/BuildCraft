package buildcraft.core.lib;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/** Provides a pool of worker threads that can execute tasks. Each task should take no longer than (ideally) 5ms or at a
 * push 30ms. Each task is watched to make sure that it takes less time to complete that that, and if it takes longer
 * then a warning is logged. */
public class BCWorkerThreads {
    private static final ExecutorService WORKER_THREAD_POOL;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int min = 0;
        int max = availableProcessors <= 3 ? 1 : 2;

        ThreadFactory factory = new BasicThreadFactory.Builder().daemon(false).namingPattern("BuildCraft Worker Thread %d").build();
        RejectedExecutionHandler rejectHandler = new CallerRunsPolicy();
        WORKER_THREAD_POOL = new ThreadPoolExecutor(min, max, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory, rejectHandler);
    }

    /** Executes a task. If this takes longer than 30ms the it assumes that something has gone wrong, and will notify
     * the log that a task took too long. If it goes on for longer than 10s then it will make a big error in the log. */
    public static void execute(Runnable task) {
        WORKER_THREAD_POOL.execute(task);
        // TODO: Add task management!
    }
}

package buildcraft.core.lib;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import buildcraft.api.core.BCLog;

/** Provides a pool of worker threads that can execute tasks. Each task should take no longer than (ideally) 5ms or at a
 * push 30ms. Each task is watched to make sure that it takes less time to complete that that, and if it takes longer
 * then a warning is logged. */
public class BCWorkerThreads {
    private static final ExecutorService WORKING_POOL, DEPENDANT_WORKING_POOL, MONITORING_POOL;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int max = Math.max(1, availableProcessors / 3);

        ThreadFactory factory = new BasicThreadFactory.Builder().daemon(false).namingPattern("BuildCraft Worker Thread %d").build();
        RejectedExecutionHandler rejectHandler = new CallerRunsPolicy();
        WORKING_POOL = new ThreadPoolExecutor(0, max, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory, rejectHandler);

        factory = new BasicThreadFactory.Builder().daemon(false).namingPattern("BuildCraft Dependant Worker Thread %d").build();
        DEPENDANT_WORKING_POOL = new ThreadPoolExecutor(0, max, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory, rejectHandler);

        factory = new BasicThreadFactory.Builder().daemon(false).namingPattern("BuildCraft Monitoring Thread %d").build();
        MONITORING_POOL = Executors.newCachedThreadPool(factory);
    }

    /** Executes a task. If this takes longer than 30ms the it assumes that something has gone wrong, and will notify
     * the log that a task took too long. If it goes on for longer than 10s then it will make a big error in the log. */
    public static void executeWorkTask(Runnable task) {
        Task taskMonitor = new Task(task);
        Future<?> future = WORKING_POOL.submit(taskMonitor);
        if (!future.isDone()) {
            executeMonitoringTask(new MonitorTask(taskMonitor, future, task.getClass()));
        }
    }

    public static void executeDependantTask(Runnable task) {
        DEPENDANT_WORKING_POOL.execute(task);
    }

    public static void executeMonitoringTask(Runnable task) {
        MONITORING_POOL.execute(task);
    }

    private static class Task implements Runnable {
        private final CountDownLatch start = new CountDownLatch(1);
        private final CountDownLatch end = new CountDownLatch(1);
        private final Runnable delegate;

        public Task(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            start.countDown();
            try {
                delegate.run();
            } finally {
                end.countDown();
            }
        }
    }

    private static class MonitorTask implements Runnable {
        private final Task task;
        private final Future<?> future;
        private final Class<?> taskType;

        public MonitorTask(Task task, Future<?> future, Class<?> taskType) {
            this.task = task;
            this.future = future;
            this.taskType = taskType;
        }

        @Override
        public void run() {
            try {
                runThrowable();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void runThrowable() throws InterruptedException {
            long startMonitor = System.currentTimeMillis();
            task.start.await();
            if (System.currentTimeMillis() - startMonitor > 30) {
                BCLog.logger.warn("A task took a long time to start! (more than 30 ms) [" + taskType.getSimpleName() + "]");
            }
            try {
                future.get(30, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e1) {
                // Ignore it- it will have been logged by the executor
            } catch (TimeoutException e) {
                BCLog.logger.warn("A task took too long! (more than 30 ms) [" + taskType.getSimpleName() + "]");
                try {
                    future.get(9970, TimeUnit.MILLISECONDS);
                } catch (ExecutionException e1) {
                    // Ignore it- it will have been logged by the executor
                } catch (TimeoutException e1) {
                    BCLog.logger.warn("A task took WAAAAY too long! (more than 10 seconds) [" + taskType.getSimpleName() + "]");
                    task.end.await();
                    BCLog.logger.info("The task FINALLY completed after " + (System.currentTimeMillis() - startMonitor) + "ms");
                }
            }
        }
    }
}

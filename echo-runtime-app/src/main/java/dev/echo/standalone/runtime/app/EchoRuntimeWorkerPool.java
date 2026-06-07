package dev.echo.standalone.runtime.app;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class EchoRuntimeWorkerPool implements AutoCloseable {
    private final ExecutorService executorService;

    public EchoRuntimeWorkerPool(int workerCount, String threadPrefix) {
        if (workerCount < 1) {
            throw new IllegalArgumentException("workerCount must be positive");
        }
        String prefix = Objects.requireNonNull(threadPrefix, "threadPrefix");
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + "-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        };
        this.executorService = Executors.newFixedThreadPool(workerCount, threadFactory);
    }

    public ExecutorService executorService() {
        return executorService;
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}

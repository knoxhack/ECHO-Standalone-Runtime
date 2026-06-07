package dev.echo.standalone.runtime.app;

import java.util.Objects;

public final class EchoRuntimeMainThread {
    private final Thread thread;

    public EchoRuntimeMainThread(Thread thread) {
        this.thread = Objects.requireNonNull(thread, "thread");
    }

    public static EchoRuntimeMainThread captureCurrent() {
        return new EchoRuntimeMainThread(Thread.currentThread());
    }

    public Thread thread() {
        return thread;
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == thread;
    }

    public void assertCurrentThread() {
        if (!isCurrentThread()) {
            throw new IllegalStateException("Operation must run on ECHO runtime main thread");
        }
    }
}

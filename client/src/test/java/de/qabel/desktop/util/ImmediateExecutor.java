package de.qabel.desktop.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ImmediateExecutor implements ExecutorService {
    private boolean shutdown;

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @NotNull
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        T result = null;
        Exception exception = null;
        try {
            result = task.call();
        } catch (Exception e) {
            exception = e;
        }

        final T finalResult = result;
        final Exception finalException = exception;
        return new Future<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                if (finalException == null) {
                    return finalResult;
                }
                throw new ExecutionException(finalException);
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
    }

    @NotNull
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return submit(() -> {task.run(); return result;});
    }

    @NotNull
    @Override
    public Future<?> submit(Runnable task) {
        return submit(() -> {task.run(); return true;});
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new IllegalStateException("not implemented");
    }

    @NotNull
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new IllegalStateException("not implemented");
    }

    @NotNull
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}

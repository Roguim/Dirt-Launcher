package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.DirtLib;
import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.Renderer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Task<T> {
    public static final int MAX_DOWNLOAD_ATTEMPTS = 12;
    private IOException exception;
    private long lastProgress;
    private long lastTime;
    protected volatile long completion;
    public final AtomicLong progress = new AtomicLong();

    public long getProgress() {
        return progress.get();
    }

    public abstract InputStream openSource() throws IOException;

    public abstract OutputStream openDestination() throws IOException;

    public abstract CompletableFuture<?> prepare();

    public abstract boolean isComplete();

    public abstract String getType();

    public abstract T runOrThrow() throws ExecutionException, InterruptedException;

    public T run() {
        try {
            return runOrThrow();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Task<?> run(Renderer renderer, String title) {
        return TaskExecutor.execute(this, renderer, title);
    }

    public abstract T getResult();

    public long getCompletion() {
        return completion;
    }

    public long pollProgress() {
        long currentTime = System.currentTimeMillis();
        final long downloaded = progress.get();
        if (downloaded <= 0 || lastProgress == downloaded) return 0;
        long currentProgress = downloaded - lastProgress;
        long timePassed = currentTime - lastTime;
        lastProgress = downloaded;
        lastTime = currentTime;
        return (currentProgress * 1000) / Math.max(1, timePassed);
    }

    public CompletableFuture<?> execute() {
        return CompletableFuture.runAsync(this::tryComplete, DirtLib.THREAD_POOL);
    }

    public boolean completedExceptionally() {
        return exception != null;
    }

    public IOException getException() {
        return exception;
    }

    public void throwException() throws IOException {
        throw exception;
    }

    protected void tryComplete(){
        exception = null;
        int attempts = 0;
        String initFailed = "Task was unable to initialize. Check " + this.getClass().getName() + ".java";
        Optional<IOException> exception = Optional.of(new IOException(initFailed));
        while (attempts++ < MAX_DOWNLOAD_ATTEMPTS && exception.isPresent()) exception = complete();
        this.exception = exception.orElse(null);
    }

    protected Optional<IOException> complete() {
        try (
                InputStream in = openSource();
                OutputStream out = openDestination()
        ) {
            byte[] buffer = new byte[1024 * 8];
            int n;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
                progress.getAndAdd(+n);
            }
            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.of(e);
        }
    }
}

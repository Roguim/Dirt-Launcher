package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.DirtLib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Task {
    public static final int MAX_DOWNLOAD_ATTEMPTS = 12;
    private long lastProgress;
    private long lastTime;
    protected volatile long completion;
    public final AtomicLong progress = new AtomicLong();

    public long getProgress() {
        return progress.get();
    }

    public abstract InputStream openSource() throws IOException;

    public abstract OutputStream openDestination() throws IOException;

    public abstract CompletableFuture<?> preExecute();

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

    protected Optional<IOException> tryComplete(){
        int attempts = 0;
        String initFailed = "Download was unable to initialize. Check " + this.getClass().getName() + ".java";
        Optional<IOException> exception = Optional.of(new IOException(initFailed));
        while (attempts++ < MAX_DOWNLOAD_ATTEMPTS && exception.isPresent()) exception = complete();
        return exception;
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
            return Optional.of(e);
        }
    }
}

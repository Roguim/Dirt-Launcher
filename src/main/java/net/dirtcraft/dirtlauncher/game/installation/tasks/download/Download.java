package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class Download {
    private final URL src;
    private final File dest;
    private final long size;
    private final AtomicLong currentProgress;
    private long lastProgress;
    private long lastTime;

    public Download(URL src, File dest, long size){
        this.currentProgress = new AtomicLong(-1);
        this.src = src;
        this.dest = dest;
        this.size = size;
    }

    public long getBytesPerSecond(){
        final long downloaded = currentProgress.get();
        if (downloaded <= 0) return 0;
        long currentProgress = downloaded - lastProgress;
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastTime;
        lastProgress = currentTime;
        lastProgress = downloaded;
        return (currentProgress * timePassed) / 1000;
    }

    /*
    private long getBytesPerSecond(long timeMs){
        synchronized (currentProgress){
            final long downloaded = currentProgress.get() - lastProgress;
            if (downloaded == 0) return 0;
            lastProgress = currentProgress.get();
            return (long) ((double) downloaded * (double)(1000/timeMs));
        }
    }
     */

    public CompletableFuture<Result> downloadAsync(ExecutorService service){
        return CompletableFuture.supplyAsync(this::download, service);
    }

    public CompletableFuture<Result> downloadAsync(){
        return CompletableFuture.supplyAsync(this::download);
    }

    public Result download(){
        this.lastTime = System.currentTimeMillis();
        currentProgress.set(0);
        Throwable e = WebUtils.tryCopyUrlToFile(src, dest, currentProgress).orElse(null);
        return new Result(e, this);
    }

    public long getProgress(){
        return currentProgress.get();
    }

    public long getSize(){
        return size;
    }

    public static class Result {
        private final Throwable e;
        private final Download value;
        private Result(Throwable e, Download value){
            this.e = e;
            this.value = value;
        }

        public boolean finishedExceptionally(){
            return e != null;
        }

        public Optional<Throwable> getException(){
            return Optional.ofNullable(e);
        }

        public File getFile(){
            return value.dest;
        }

        public URL getSource(){
            return value.src;
        }

        public long getSize(){
            return value.size;
        }

    }
}

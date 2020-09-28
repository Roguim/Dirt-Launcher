package net.dirtcraft.dirtlauncher.game.installation;


import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DownloadManager implements AutoCloseable {
    public final static long KILOBYTE = 1024;
    public final static long MEGABYTE = KILOBYTE * 1024;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Constants.MAX_DOWNLOAD_THREADS);
    private final Consumer<Progress> progressTracker;
    private final AtomicBoolean operating;
    private final Collection<Download> downloads;
    private final List<CompletableFuture<File>> tasks;
    private final long interval;
    public DownloadManager(Consumer<Progress> progressConsumer, long updateTimeMs){
        this.interval = updateTimeMs;
        this.progressTracker = progressConsumer;
        tasks = new ArrayList<>();
        operating = new AtomicBoolean(true);
        downloads =  Collections.synchronizedList(new ArrayList<>());
        CompletableFuture.runAsync(()->{
            while (operating.get()) updateTracker();
        }).exceptionally(e->{
            e.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<File> download(URL url, File file){
        CompletableFuture<File> task = CompletableFuture.supplyAsync(()->{
            long size = WebUtils.getFileSize(url);
            Download download = new Download(url, file, size);
            downloads.add(download);
            return download;
        }).thenApplyAsync(Download::download, threadPool);
        tasks.add(task);
        return task;
    }

    public CompletableFuture<File> download(String url, File file, long size) {
        URL src = MiscUtils.getURL(url).orElse(null);
        Download download = new Download(src, file, size);
        downloads.add(download);
        CompletableFuture<File> task = CompletableFuture.supplyAsync(download::download, threadPool);
        tasks.add(task);
        return task;
    }

    @Override
    public void close() {
        operating.set(false);
        updateTracker();
    }

    public void waitUntilDownloaded(){
        tasks.forEach(CompletableFuture::join);
    }

    private void updateTracker(){
        long x = System.currentTimeMillis();

        final Download[] downloads = this.downloads.toArray(new Download[]{});
        final Progress progressContainer = new Progress(downloads, interval);

        progressTracker.accept(progressContainer);

        long toSleep = interval - (System.currentTimeMillis() - x);
        MiscUtils.trySleep(toSleep);
    }

    public static class Download{
        final long size;
        private final URL src;
        private final File dest;
        final AtomicLong currentProgress;
        volatile long lastProgress;

        private Download(URL src, File dest, long size){
            this.currentProgress = new AtomicLong(0);
            this.src = src;
            this.dest = dest;
            this.size = size;
        }

        private long getBytesPerSecond(long timeMs){
            synchronized (currentProgress){
                final long downloaded = currentProgress.get() - lastProgress;
                if (downloaded == 0) return 0;
                lastProgress = currentProgress.get();
                return (long) ((double) downloaded * (double)(1000/timeMs));
            }
        }

        private File download(){
            WebUtils.tryCopyUrlToFile(src, dest, currentProgress);
            return dest;
        }
    }

    public static class Progress{
        public final long progress;
        public final long totalSize;
        public final long bytesPerSecond;
        public final int files;
        public Progress(Download[] downloads, long interval){
            this.progress = Arrays.stream(downloads).mapToLong(d->d.currentProgress.get()).sum();
            this.totalSize = Arrays.stream(downloads).mapToLong(d->d.size).sum();
            this.bytesPerSecond = Arrays.stream(downloads).mapToLong(d->d.getBytesPerSecond(interval)).sum();
            this.files = downloads.length;
        }

        public long getBytesPerSecond(){
            return bytesPerSecond;
        }

        public double getPercent(){
            if (totalSize == 0) return 0;
            return progress / (double) totalSize;
        }
    }
}

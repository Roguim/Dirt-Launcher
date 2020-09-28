package net.dirtcraft.dirtlauncher.game.installation;


import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DownloadManager implements AutoCloseable {
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Constants.MAX_DOWNLOAD_THREADS);
    private final Consumer<Progress> downloadProgress;
    private final long interval;
    private final AtomicBoolean operating;
    private final Map<Thread, Download> downloads;
    public DownloadManager(Consumer<Progress> progressConsumer, long updateTimeMs){
        this.interval = updateTimeMs;
        this.downloadProgress = progressConsumer;
        operating = new AtomicBoolean(true);
        downloads = new ConcurrentHashMap<>();
        CompletableFuture.runAsync(()->{
            while (operating.get()){
                long x = System.currentTimeMillis();
                Collection<Download> downloads = this.downloads.values();
                long progress = downloads.stream().mapToLong(d->d.currentProgress.get()).sum();
                long total = downloads.stream().mapToLong(d->d.size).sum();
                long kbps = (long) downloads.stream().mapToLong(d->d.getKBps(interval)).average().orElse(-1d);
                downloadProgress.accept(new Progress(progress, total, kbps));
                System.out.println(String.format("Downloading.. %d / %d (%d%% @ %dKB/s)", progress, total, progress*100/total, kbps));
                try{
                    Thread.sleep(System.currentTimeMillis() + interval - x);
                } catch (Exception ignored){}
            }
        });
    }

    public CompletableFuture<File> download(URL url, File file){
        return CompletableFuture.supplyAsync(()->{
            long size = WebUtils.getFileSize(url);
            Download download = new Download(size);
            downloads.put(Thread.currentThread(), download);
            WebUtils.tryCopyUrlToFile(url, file, download.currentProgress);
            return file;
        }, threadPool);
    }

    @Override
    public void close() throws Exception {
        operating.set(false);
    }

    public static class Download{
        final AtomicLong currentProgress;
        volatile long lastProgress;
        final long size;

        private Download(long size){
            this.size = size;
            this.currentProgress = new AtomicLong(0);
        }

        private long getKBps(long timeMs){
            synchronized (currentProgress){
                final long downloaded = currentProgress.get() - lastProgress;
                lastProgress = currentProgress.get();
                return downloaded / (timeMs/1000);
            }
        }
    }

    public static class Progress{
        public final long progress;
        public final long size;
        public final long kbps;
        public Progress(long progress, long size, long kbps){
            this.progress = progress;
            this.size = size;
            this.kbps = kbps;
        }
    }
}

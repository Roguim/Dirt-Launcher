package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import com.google.gson.stream.JsonReader;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DownloadManager {
    private static ExecutorService threadPool = Main.getIOExecutor();
    private final Timer scheduler = new Timer();

    public List<Download.Result> download(Consumer<ProgressDetailed> progressConsumer, DownloadInfo downloadMeta){
        return download(progressConsumer, Collections.singletonList(downloadMeta));
    }

    public List<Download.Result> download(Consumer<ProgressDetailed> progressConsumer, List<DownloadInfo> downloadMeta) {
        List<Download> downloads = calculateDownloads(downloadMeta, progressConsumer);
        return executeDownloads(downloads, progressConsumer);
    }

    public <T,R> List<R> download(List<T> batch, Function<T,R> func,Consumer<ProgressBasic> progressTracker){
        AtomicInteger completed = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        ProgressBasic progress = new ProgressBasic(completed, batch.size(), counter.addAndGet(1));
        TimerTask task = MiscUtils.toTimerTask(()->progressTracker.accept(progress));
        try {
            scheduler.scheduleAtFixedRate(task, 0, 50);
            List<CompletableFuture<R>> futures = batch.stream()
                    .map(t -> CompletableFuture.supplyAsync(() -> func.apply(t)))
                    .collect(Collectors.toList());
            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            task.cancel();
            task.run();
        }
    }

    public ExecutorService getThreadPool(){
        //((ThreadPoolExecutor)threadPool).setCorePoolSize();
        return threadPool;
    }

    private List<Download> calculateDownloads(List<DownloadInfo> downloadMeta, Consumer<ProgressDetailed> progressConsumer){
        AtomicInteger counter = new AtomicInteger();
        final AtomicInteger progress = new AtomicInteger();
        final TimerTask updater = MiscUtils.toTimerTask(()->progressConsumer.accept(new ProgressDetailed(progress, downloadMeta.size(), counter.getAndIncrement())));
        try {
            scheduler.scheduleAtFixedRate(updater, 0, 50);
            final List<CompletableFuture<Download>> infoFutures = downloadMeta.stream()
                    .map(dl -> dl.getDownloadAsync(threadPool).whenComplete((t,e)->progress.addAndGet(1)))
                    .collect(Collectors.toList());
            return infoFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            updater.cancel();
            updater.run();
        }
    }

    private List<Download.Result> executeDownloads(List<Download> downloads, Consumer<ProgressDetailed> progressConsumer){
        final BitrateSmoother bitrateSmoother = new BitrateSmoother(40);
        AtomicInteger counter = new AtomicInteger();
        final TimerTask updater = MiscUtils.toTimerTask(()->progressConsumer.accept(new ProgressDetailed(downloads.toArray(new Download[]{}), bitrateSmoother, counter.getAndIncrement())));
        try {
            scheduler.scheduleAtFixedRate(updater, 0, 50);
            final List<CompletableFuture<Download.Result>> futureResults = downloads.stream().map(dl -> dl.downloadAsync(threadPool)).collect(Collectors.toList());
            return futureResults.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            updater.cancel();
            updater.run();
        }

    }

    public static class BitrateSmoother{
        private long[] bytesPerSecond;
        private int counter;
        private int samples;
        public BitrateSmoother(int samples){
            this.counter = 0;
            this.bytesPerSecond = new long[samples];
            Arrays.fill(bytesPerSecond, 0);
            this.samples = samples;
        }

        public long getAveraged(long i){
            int count = counter == Integer.MAX_VALUE? 0 : counter++;
            bytesPerSecond[count % samples] = i;
            return (long) Arrays.stream(bytesPerSecond).average().orElse(0d);
        }
    }
}

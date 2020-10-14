package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.DownloadTask;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IFileDownload;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.Result;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.ProgressBasic;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.ProgressDetailed;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.progress.Trackers;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DownloadManager {
    private static ExecutorService threadPool = Main.getIOExecutor();
    private final Timer scheduler = new Timer();

    public ExecutorService getThreadPool(){
        return threadPool;
    }

    public List<Result> download(Trackers.MultiUpdater progressConsumer, IDownload downloadMeta, Path folder){
        return download(progressConsumer, Collections.singletonList(downloadMeta), folder);
    }

    public List<Result> download(Trackers.MultiUpdater progressConsumer, IFileDownload downloadMeta){
        return download(progressConsumer, Collections.singletonList(downloadMeta));
    }

    public List<Result> download(Trackers.MultiUpdater progressConsumer, List<IDownload> downloadMeta, Path folder){
        return executeDownloads(preCalculate(progressConsumer, downloadMeta, folder), progressConsumer);
    }

    public List<Result> download(Trackers.MultiUpdater progressConsumer, List<IFileDownload> downloadMeta) {
        return executeDownloads(preCalculate(progressConsumer, downloadMeta), progressConsumer);
    }

    public List<Result> download(Trackers.DownloadUpdater progressConsumer, Collection<DownloadTask> downloads){
        return executeDownloads(downloads, progressConsumer);
    }

    public List<DownloadTask> preCalculate(Trackers.PreparationUpdater progressConsumer, List<IDownload> downloadMeta, Path folder){
        return calculateDownloads(downloadMeta, progressConsumer, dl->dl.getDownload(folder));
    }

    public List<DownloadTask> preCalculate(Trackers.PreparationUpdater progressConsumer, List<IFileDownload> downloadMeta) {
        return calculateDownloads(downloadMeta, progressConsumer, IFileDownload::getDownload);
    }

    private <T extends IDownload> List<DownloadTask> calculateDownloads(List<T> downloadMeta, Trackers.PreparationUpdater progressConsumer, Function<T, DownloadTask> function){
        AtomicInteger counter = new AtomicInteger();
        final AtomicInteger progress = new AtomicInteger();
        final TimerTask updater = MiscUtils.toTimerTask(()-> progressConsumer.sendUpdate(new ProgressBasic(progress, downloadMeta.size(), counter.getAndIncrement())));
        final Function<T,T> getSizeFunc = meta->{
            meta.setSize(meta.getSize() > 0? meta.getSize() : getFileSize(meta.getUrl()));
            return meta;
        };
        try {
            scheduler.scheduleAtFixedRate(updater, 0, 50);
            final List<CompletableFuture<DownloadTask>> infoFutures = downloadMeta.stream()
                    .map(meta->CompletableFuture.supplyAsync(()->getSizeFunc.apply(meta), threadPool))
                    .map(f->f.thenApply(function))
                    .map(f->f.whenComplete((t,e)->progress.addAndGet(1)))
                    .collect(Collectors.toList());
            return infoFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            updater.cancel();
            updater.run();
        }
    }

    private List<Result> executeDownloads(Collection<DownloadTask> downloads, Trackers.DownloadUpdater progressConsumer){
        final BitrateSmoother bitrateSmoother = new BitrateSmoother(40);
        AtomicInteger counter = new AtomicInteger();
        final TimerTask updater = MiscUtils.toTimerTask(()->progressConsumer.sendUpdate(new ProgressDetailed(downloads.toArray(new DownloadTask[]{}), bitrateSmoother, counter.getAndIncrement())));
        try {
            scheduler.scheduleAtFixedRate(updater, 0, 50);
            final List<CompletableFuture<Result>> futureResults = downloads.stream().map(dl -> dl.downloadAsync(threadPool)).collect(Collectors.toList());
            return futureResults.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            updater.cancel();
            updater.run();
        }

    }

    private long getFileSize(URL url){
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getContentLengthLong();
        } catch (IOException e) {
            Logger.INSTANCE.verbose(e);
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
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

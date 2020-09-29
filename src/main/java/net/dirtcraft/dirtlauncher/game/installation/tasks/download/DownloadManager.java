package net.dirtcraft.dirtlauncher.game.installation.tasks.download;


import javafx.beans.NamedArg;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DownloadManager {
    private static ExecutorService threadPool = Executors.newFixedThreadPool(Constants.MAX_DOWNLOAD_THREADS);
    private final List<DownloadInfo> downloads = new ArrayList<>(); //todo Thread-local maybe?
    private final Timer scheduler = new Timer();

    public List<Download.Result> download(Consumer<Progress> progressConsumer, long updateInterval){
        try {
            List<Download> downloads = calculateDownloads();
            return executeDownloads(downloads, progressConsumer, updateInterval);
        } finally {
            clearDownloads();
        }
    }

    private List<Download> calculateDownloads(){
        final List<CompletableFuture<Download>> infoFutures = this.downloads.stream()
                .map(dl->dl.getDownloadAsync(threadPool))
                .collect(Collectors.toList());
        return infoFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private List<Download.Result> executeDownloads(List<Download> downloads, Consumer<Progress> progressConsumer, long updateInterval){
        final TimerTask updater = MiscUtils.toTimerTask(()->progressConsumer.accept(new Progress(downloads.toArray(new Download[]{}))));
        try {
            scheduler.scheduleAtFixedRate(updater, 250, updateInterval);
            final List<CompletableFuture<Download.Result>> futureResults = downloads.stream().map(dl -> dl.downloadAsync(threadPool)).collect(Collectors.toList());
            return futureResults.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            updater.run();
            updater.cancel();
        }

    }

    public void addDownload(DownloadInfo downloadInfo){
        downloads.add(downloadInfo);
    }

    public void addDownloads(Collection<DownloadInfo> downloadInfo){
        downloads.addAll(downloadInfo);
    }

    public void addDownload(@NamedArg("src") URL src, @NamedArg("dest") File dest){
        downloads.add(new DownloadInfo.Default(src, dest));
    }

    public void addDownload(@NamedArg("src") String src, @NamedArg("dest") File dest){
        addDownload(MiscUtils.getURL(src).orElse(null), dest);
    }

    public void addDownload(@NamedArg("src") URL src, @NamedArg("dest") File dest, @NamedArg("size") long size){
        downloads.add(new DownloadInfo.Default(src, dest, size));
    }

    public void addDownload(@NamedArg("src") String src, @NamedArg("dest") File dest, @NamedArg("size") long size){
        addDownload(MiscUtils.getURL(src).orElse(null), dest, size);
    }

    public void clearDownloads(){
        downloads.clear();
    }

    public static class Progress{
        public final long progress;
        public final long totalSize;
        public final long bytesPerSecond;
        public final int files;
        public Progress(Download[] downloads){
            this.bytesPerSecond = Arrays.stream(downloads).mapToLong(Download::getBytesPerSecond).sum();
            this.progress = Arrays.stream(downloads).mapToLong(Download::getProgress).sum();
            this.totalSize = Arrays.stream(downloads).mapToLong(Download::getSize).sum();
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

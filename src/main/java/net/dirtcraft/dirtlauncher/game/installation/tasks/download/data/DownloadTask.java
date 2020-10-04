package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask {
    private IDownload downloadData;
    private Path folder;
    private final AtomicLong currentProgress;
    private long lastProgress;
    private long lastTime;

    public DownloadTask(IDownload meta, Path folder){
        this.currentProgress = new AtomicLong(-1);
        this.downloadData = meta;
        this.folder = folder;
    }

    public DownloadTask(IPresetDownload meta){
        this.currentProgress = new AtomicLong(-1);
        this.downloadData = meta;
        this.folder = meta.getFolder();
    }

    public long getBytesPerSecond(){
        long currentTime = System.currentTimeMillis();
        final long downloaded = currentProgress.get();
        if (downloaded <= 0 || lastProgress == downloaded) return 0;
        long currentProgress = downloaded - lastProgress;
        long timePassed = currentTime - lastTime;
        lastProgress = downloaded;
        lastTime = currentTime;
        return (currentProgress * 1000) / timePassed;
    }

    public Result download(){
        this.lastTime = System.currentTimeMillis();
        currentProgress.set(0);
        Throwable e = WebUtils.tryCopyUrlToFile(downloadData.getUrl(), getFile(), currentProgress).orElse(null);
        return new Result(e, downloadData, folder);
    }

    public CompletableFuture<Result> downloadAsync(){
        return CompletableFuture.supplyAsync(this::download);
    }

    public CompletableFuture<Result> downloadAsync(ExecutorService threadPool){
        return CompletableFuture.supplyAsync(this::download, threadPool);
    }

    public long getProgress(){
        return currentProgress.get();
    }

    public long getSize(){
        return downloadData.getSize();
    }

    public File getFile() {
        return folder.resolve(downloadData.getFileName()).toFile();
    }
}

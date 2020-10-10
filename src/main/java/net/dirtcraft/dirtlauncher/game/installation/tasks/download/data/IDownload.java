package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IDownload {
    default DownloadTask getDownload(Path folder){
        if (getSize() <= 0) setSize(WebUtils.getFileSize(getUrl()));
        return new DownloadTask(this, folder);
    }

    default CompletableFuture<DownloadTask> getDownloadAsync(Path folder){
        return CompletableFuture.supplyAsync(()->getDownload(folder));
    }

    default CompletableFuture<DownloadTask> getDownloadAsync(Path folder, ExecutorService threadPool){
        return CompletableFuture.supplyAsync(()->getDownload(folder), threadPool);
    }

    default IPresetDownload getPreset(File dest){
        return new DownloadMeta(getUrl(), dest, getSize());
    }

    default IPresetDownload getPreset(Path folder){
        return new DownloadMeta(getUrl(), folder.resolve(getFileName()).toFile(), getSize());
    }

    long getSize();

    void setSize(long size);

    URL getUrl();

    String getFileName();
}

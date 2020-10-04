package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IPresetDownload extends IDownload {
    default DownloadTask getDownload(){
        return new DownloadTask(this, getFolder());
    }

    default CompletableFuture<DownloadTask> getDownloadAsync(){
        return CompletableFuture.supplyAsync(this::getDownload);
    }

    default CompletableFuture<DownloadTask> getDownloadAsync(ExecutorService threadPool){
        return CompletableFuture.supplyAsync(this::getDownload, threadPool);
    }

    Path getFolder();
}

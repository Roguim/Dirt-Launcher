package net.dirtcraft.dirtlauncher.game.installation.tasks.download;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface DownloadInfo {

    Download getDownload();

    default CompletableFuture<Download> getDownloadAsync(ExecutorService executor){
        return CompletableFuture.supplyAsync(this::getDownload, executor);
    }

    default CompletableFuture<Download> getDownloadAsync(){
        return CompletableFuture.supplyAsync(this::getDownload);
    }

    class Default implements DownloadInfo {
        private final URL src;
        private final File dest;
        private long size;

        public Default(URL src, File dest){
            this.src = src;
            this.dest = dest;
            this.size = -1;
        }

        public Default(URL src, File dest, long size){
            this.src = src;
            this.dest = dest;
            this.size = size;
        }

        @Override
        public Download getDownload() {
            if (size == -1) return new Download(src,dest, WebUtils.getFileSize(src));
            else return new Download(src, dest, size);
        }
    }
}

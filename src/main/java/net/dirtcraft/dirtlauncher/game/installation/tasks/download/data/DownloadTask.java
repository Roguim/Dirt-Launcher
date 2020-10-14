package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static net.dirtcraft.dirtlauncher.configuration.Constants.MAX_DOWNLOAD_ATTEMPTS;

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

    public DownloadTask(IFileDownload meta){
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
        Throwable e = tryCopyUrlToFile(downloadData.getUrl(), getFile()).orElse(null);
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

    private Optional<IOException> tryCopyUrlToFile(URL url, File file){
        int attempts = 0;
        String initFailed = "Download was unable to initialize. Check " + this.getClass().getName() + ".java";
        Optional<IOException> exception = Optional.of(new IOException(initFailed));
        while (attempts++ < MAX_DOWNLOAD_ATTEMPTS && exception.isPresent()) exception = copyUrlToFile(url, file);
        return exception;
    }

    private Optional<IOException> copyUrlToFile(URL url, File file) {
        try (
                InputStream in = url.openStream();
                OutputStream out = FileUtils.openOutputStream(file)
        ) {
            byte[] buffer = new byte[1024 * 8];
            int n;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
                currentProgress.getAndAdd(+n);
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }
}

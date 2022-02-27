package net.dirtcraft.dirtlauncher.lib.data.tasks;

import net.dirtcraft.dirtlauncher.lib.DirtLib;
import net.dirtcraft.dirtlauncher.lib.config.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class DownloadTask extends FileTask {
    public final URL src;

    public DownloadTask(URL src, File destination) {
        this(src, destination, -0, null);
    }

    public DownloadTask(URL src, File destination, int size) {
        this(src, destination, size, null);
    }

    public DownloadTask(URL src, File destination, String sha1) {
        this(src, destination, -0, sha1);
    }

    public DownloadTask(URL src, File destination, long size, String sha1) {
        super(destination, size, sha1);
        this.src = src;
    }

    public URL getSrc(){
        return src;
    }

    @Override
    public InputStream openSource() throws IOException {
        return src.openStream();
    }

    @Override
    public CompletableFuture<?> preExecute() {
        if (this.completion > 0) return Constants.COMPLETED_FUTURE;
        return CompletableFuture.runAsync(()->{
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) src.openConnection();
                conn.setRequestMethod("HEAD");
                this.completion = conn.getContentLengthLong();
            } catch (IOException e) {
                //Logger.INSTANCE.verbose(e);
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }, DirtLib.THREAD_POOL);
    }
}

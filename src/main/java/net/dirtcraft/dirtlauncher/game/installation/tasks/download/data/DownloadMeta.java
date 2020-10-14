package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public class DownloadMeta implements IFileDownload {
    private final URL src;
    private final File dest;
    private long size;

    public DownloadMeta(String src, File dest){
        this.src = MiscUtils.getURL(src).orElse(null);
        this.dest = dest;
        this.size = -1;
    }

    public DownloadMeta(URL src, File dest){
        this.src = src;
        this.dest = dest;
        this.size = -1;
    }

    public DownloadMeta(String src, File dest, long size){
        this.src = MiscUtils.getURL(src).orElse(null);
        this.dest = dest;
        this.size = size;
    }

    public DownloadMeta(URL src, File dest, long size){
        this.src = src;
        this.dest = dest;
        this.size = size;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public URL getUrl() {
        return src;
    }

    @Override
    public Path getFolder() {
        return dest.getParentFile().toPath();
    }

    @Override
    public String getFileName() {
        return dest.getName();
    }
}

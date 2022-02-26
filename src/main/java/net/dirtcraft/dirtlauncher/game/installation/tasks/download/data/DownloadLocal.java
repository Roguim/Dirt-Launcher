package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import net.dirtcraft.dirtlauncher.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DownloadLocal implements IFileDownload {
    private final File src;
    private final File dst;

    public DownloadLocal(File src, File dst) {
        this.src = src;
        this.dst = dst;
    }


    @Override
    public DownloadTask getDownload(){
        return new DownloadTask(this, getFolder()){
            @Override
            public Result download(){
                Exception e = null;
                try {
                    Files.move(src.toPath(), dst.toPath());
                } catch (IOException x) {
                    e = x;
                }
                return new Result(e, this.downloadData, ((DownloadLocal)this.downloadData).getFolder());
            }
        };
    }

    @Override
    public long getSize() {
        return src.length();
    }

    @Override
    public void setSize(long size) {

    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public String getFileName() {
        return dst.getName();
    }

    @Override
    public Path getFolder() {
        return dst.getParentFile().toPath();
    }

    @Override
    public Optional<String> getSha1() {
        return Optional.empty();
    }

    @Override
    public boolean verify() {
        return dst.exists() && dst.length() == src.length();
    }
}

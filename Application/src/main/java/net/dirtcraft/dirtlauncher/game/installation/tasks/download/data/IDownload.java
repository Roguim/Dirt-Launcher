package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public interface IDownload {
    default DownloadTask getDownload(Path folder) {
        if (getUrl() == null) return null;
        else return new DownloadTask(this, folder);
    }

    default IFileDownload getPreset(File dest){
        return new DownloadMeta(getUrl(), dest, getSize());
    }

    default IFileDownload getPreset(Path folder){
        return new DownloadMeta(getUrl(), folder.resolve(getFileName()).toFile(), getSize());
    }

    long getSize();

    void setSize(long size);

    URL getUrl();

    String getFileName();
}

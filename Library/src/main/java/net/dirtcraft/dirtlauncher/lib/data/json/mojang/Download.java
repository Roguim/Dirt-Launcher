package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;

import java.io.File;

public class Download extends FileDownload{
    public Download(int i) throws InstantiationException{
        super(0);
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }

    public DownloadTask getDownload(File destination) {
        return new DownloadTask(getUrl(), new File(destination, path), size, sha1);
    }

    public final String path;
}

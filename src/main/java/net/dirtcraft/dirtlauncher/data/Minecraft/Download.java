package net.dirtcraft.dirtlauncher.data.Minecraft;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;

import java.net.URI;
import java.net.URL;

public class Download extends FileDownload implements IDownload {
    public Download(int i) throws InstantiationException{
        super(0);
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }

    final URI path;

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {

    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getFileName() {
        return path.getPath();
    }
}

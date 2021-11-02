package net.dirtcraft.dirtlauncher.data.Minecraft;

import java.net.URL;

public class FileDownload {
    public FileDownload(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    public final String sha1;
    public final long size;
    public final URL url;
}

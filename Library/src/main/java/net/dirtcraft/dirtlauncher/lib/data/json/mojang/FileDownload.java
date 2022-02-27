package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

import java.net.MalformedURLException;
import java.net.URL;

public class FileDownload {
    public FileDownload(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    public final String sha1;
    public final long size;
    public final String url;

    public URL getUrl() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}

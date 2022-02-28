package net.dirtcraft.dirtlauncher.lib.data.json.ftb;

import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class FTBFile {
    private FTBFile(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final String version;
    public final String path;
    public final String url;
    public final String sha1;
    public final int size;
    public final List<String> tags;
    public final boolean clientonly;
    public final boolean serveronly;
    public final int id;
    public final String name;
    public final int updated;

    public DownloadTask getDownload(File modsFolder){
        try{
            return new DownloadTask(new URL(url), new File(modsFolder, path), size, sha1);
        } catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
    }

}

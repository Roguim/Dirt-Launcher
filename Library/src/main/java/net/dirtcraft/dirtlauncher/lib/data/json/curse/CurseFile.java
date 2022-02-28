package net.dirtcraft.dirtlauncher.lib.data.json.curse;

import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseFile {
    protected CurseFile(int i) throws InstantiationException{
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final long id;
    public final String displayName;
    public final String fileName;
    //public final ??? fileDate <- timestamp? useless anyway.
    public final long fileLength;
    public final int releaseType;
    //public final int fileStatus;
    public final String downloadUrl;
    //public final boolean isAlternate;
    //public final int alternateFileId;
    //public final Object dependencies;
    public final boolean isAvailable;
    //public final Object modules;
    //public final long packageFingerPrint;
    //public final String[] gameVersion;
    //public final null installMetadata;
    public final long serverPackFileId;
    //public final boolean hasInstallScript;
    //public final ??? gameVersionDateReleased;
    //public final null gameVersionFlavor

    public final boolean isServerPack; //<-- ??? modpack only?
    public final long projectId; // <-- ??? modpack only?

    public DownloadTask getDownload(File modsFolder){
        try{
            return new DownloadTask(getUrl(), new File(modsFolder, fileName), fileLength, null);
        } catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
    }

    public URL getUrl() throws MalformedURLException {
        return new URL(downloadUrl.replaceAll("\\s", "%20"));
    }
}

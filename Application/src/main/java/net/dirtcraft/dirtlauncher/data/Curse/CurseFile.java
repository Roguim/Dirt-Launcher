package net.dirtcraft.dirtlauncher.data.Curse;


import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseFile implements IDownload {
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

    public CompletableFuture<Void> downloadAsync(File modsFolder, Executor executor){
        return CompletableFuture.runAsync(()-> download(modsFolder), executor);
    }

    private void download(File modsFolder){
        try{
            WebUtils.copyURLToFile(downloadUrl.replaceAll("\\s", "%20"), new File(modsFolder, fileName));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public URL getUrl() {
        return MiscUtils.getURL(downloadUrl.replaceAll("\\s", "%20")).orElse(null);
    }

    @Override
    public long getSize() {
        return fileLength;
    }

    @Override
    public void setSize(long size) {

    }

    @Override
    public String getFileName(){
        return fileName;
    }
/*
    releaseTypes:
        1: release
        2: beta
        3: alpha
 */
}

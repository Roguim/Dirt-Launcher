package net.dirtcraft.dirtlauncher.data.Curse;


import net.dirtcraft.dirtlauncher.game.installation.tasks.download.Download;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseFile {
    private CurseFile(int i) throws InstantiationException{
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    final long id;
    final String displayName;
    final String fileName;
    //final ??? fileDate <- timestamp? useless anyway.
    final long fileLength;
    final int releaseType;
    //final int fileStatus;
    final String downloadUrl;
    //final boolean isAlternate;
    //final int alternateFileId;
    //final Object dependencies;
    final boolean isAvailable;
    //final Object modules;
    //final long packageFingerPrint;
    //final String[] gameVersion;
    //final null installMetadata;
    final long serverPackFileId;
    //final boolean hasInstallScript;
    //final ??? gameVersionDateReleased;
    //final null gameVersionFlavor

    final boolean isServerPack; //<-- ??? modpack only?
    final long projectId; // <-- ??? modpack only?

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

    public Download getDownload(File file){
        return new Download(MiscUtils.getURL(downloadUrl).orElse(null), file, fileLength);
    }

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

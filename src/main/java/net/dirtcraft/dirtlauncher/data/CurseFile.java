package net.dirtcraft.dirtlauncher.data;


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
    final int releaseType;
    final String downloadUrl;
    final boolean isAvailable;
    final boolean isServerPack;
    final long serverPackFileId;
    final long projectId;
    final long fileLength;

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

    /*"releaseType":
1: release
2: beta
3: alpha
        "dependencies": [],
        "restrictProjectFileAccess": 1,
        "isAlternate": false,
        "alternateFileId": 0,
        "fileStatus": 4,
        "fileDate": "2020-02-13T00:38:17.903Z",
        "gameVersion": [
          "1.12.2"
        ],
        "sortableGameVersion": [
    {
        "gameVersionPadded": "0000000001.0000000012.0000000002",
            "gameVersion": "1.12.2",
            "gameVersionReleaseDate": "2017-09-18T05:00:00Z",
            "gameVersionName": "1.12.2"
    }
        ],
        "modules": [
          {
            "foldername": "manifest.json",
            "fingerprint": 381212442,
            "type": 3
          }
        ],

     */
}

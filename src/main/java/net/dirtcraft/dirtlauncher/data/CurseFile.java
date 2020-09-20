package net.dirtcraft.dirtlauncher.data;


import java.io.InvalidObjectException;
import java.net.URL;

public class CurseFile {
    private CurseFile(int i) throws InvalidObjectException{
        throw new InvalidObjectException("This is a data class intended to only be constructed by GSON.");
    }

    final long id;
    final String displayName;
    final String fileName;
    final int releaseType;
    final URL downloadUrl;
    final boolean isAvailable;
    final boolean isServerPack;
    final long serverPackFileId;
    final long projectId;
    final long fileLength;

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

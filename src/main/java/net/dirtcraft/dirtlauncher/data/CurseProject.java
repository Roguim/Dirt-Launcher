package net.dirtcraft.dirtlauncher.data;

import net.dirtcraft.dirtlauncher.configuration.Constants;

import java.io.InvalidObjectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;

public class CurseProject {
    private CurseProject(int i) throws InvalidObjectException {
        throw new InvalidObjectException("This is a data class intended to only be constructed by GSON.");
    }
    public final long id;
    public final String name;
    public final String summary;
    public final HashSet<String> gameVersion;
    public final long defaultFileId;
    public final URL websiteUrl;

    private Optional<URL> getLatest(String projectId, String fileId){
        final String link = String.format(Constants.CURSE_API_URL + "%s/file/%s/download-url", projectId, fileId);
        try {
            return Optional.of(new URL(link));
        } catch (MalformedURLException e){
            return Optional.empty();
        }
    }
}









































    //public final List<CurseGameVersionLatestFile> gameVersionLatestFiles;
    /*
    public class CurseGameVersionLatestFile {
        private CurseGameVersionLatestFile(int i){
            throw new InvalidStateException("This is a data class intended to only be constructed by GSON.");
        }
        public final String gameVersion;
        public final long projectFileId;
        public final String projectFileName;
        public final int fileType;
    }

    -- left over json data that could be used for shit in the future --

        "authors": [
  {
    "name": "darkosto",
    "url": "https://www.curseforge.com/members/15754032-darkosto?username=darkosto",
    "projectId": 303207,
    "id": 254496,
    "projectTitleId": 9,
    "projectTitleTitle": "Pack Developer",
    "userId": 15754032,
    "twitchId": 63937599
  }
],
"attachments": [
  {
    "id": 171313,
    "projectId": 303207,
    "description": "",
    "isDefault": false,
    "thumbnailUrl": "https://media.forgecdn.net/avatars/thumbnails/171/313/256/256/636728316228853484.jpeg",
    "title": "636728316228853484.jpeg",
    "url": "https://media.forgecdn.net/avatars/171/313/636728316228853484.jpeg",
    "status": 1
  },
  {
    "id": 236183,
    "projectId": 303207,
    "description": "",
    "isDefault": true,
    "thumbnailUrl": "https://media.forgecdn.net/avatars/thumbnails/236/183/256/256/637090361160027737.png",
    "title": "637090361160027737.png",
    "url": "https://media.forgecdn.net/avatars/236/183/637090361160027737.png",
    "status": 1
  }
],


     */

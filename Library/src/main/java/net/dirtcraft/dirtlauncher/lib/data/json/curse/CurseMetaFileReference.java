package net.dirtcraft.dirtlauncher.lib.data.json.curse;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.tasks.JsonTask;

import java.net.MalformedURLException;
import java.net.URL;

public class CurseMetaFileReference {
    private CurseMetaFileReference(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }

    public final long projectID;
    public final long fileID;
    public final boolean required;

    public String getDownloadUrl(){
        return String.format(Constants.CURSE_API_URL + "%s/file/%s", projectID, fileID);
    }

    public JsonTask<CurseFile> getManifest(){
        try {
            return new JsonTask<>(new URL(getDownloadUrl()), CurseFile.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean equals(CurseMetaFileReference o){
        return projectID == o.projectID && fileID == o.fileID && required == o.required;
    }

    public boolean isRequired(){
        return required;
    }
}

package net.dirtcraft.dirtlauncher.data;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseMetaFileReference {
    private CurseMetaFileReference(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final long projectID;
    public final long fileID;
    public final boolean required;

    public String getUrl(){
        return String.format(Constants.CURSE_API_URL + "%s/file/%s", projectID, fileID);
    }

    public CompletableFuture<CurseFile2> getManifestAsync(Executor executor){
        return CompletableFuture.supplyAsync(()->{
            @SuppressWarnings("UnstableApiUsage")
            TypeToken<CurseFile2> token = new TypeToken<CurseFile2>(){};
            return WebUtils.getGsonFromUrl(getUrl(), token).orElse(null);
        }, executor);
    }

    public boolean equals(CurseMetaFileReference o){
        return projectID == o.projectID && fileID == o.fileID;
    }

}

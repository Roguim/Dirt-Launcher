package net.dirtcraft.dirtlauncher.data.Curse;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseMetaFileReference implements IDownload {
    private CurseMetaFileReference(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    private transient @Nullable CurseFile manifest;
    public final long projectID;
    public final long fileID;
    public final boolean required;

    public String getDownloadUrl(){
        return String.format(Constants.CURSE_API_URL + "%s/file/%s", projectID, fileID);
    }

    public CompletableFuture<CurseFile> getManifestAsync(Executor executor){
        return CompletableFuture.supplyAsync(this::getManifest, executor);
    }

    @SuppressWarnings("UnstableApiUsage")
    private CurseFile getManifest(){
        if (this.manifest != null) return manifest;
        final TypeToken<CurseFile> type = new TypeToken<CurseFile>(){};
        return (manifest = WebUtils.getGsonFromUrl(getDownloadUrl(), type).orElse(null));
    }

    public boolean equals(CurseMetaFileReference o){
        return projectID == o.projectID && fileID == o.fileID;
    }

    public boolean isRequired(){
        return required;
    }

    @Override
    public long getSize() {
        return getManifest().getSize();
    }

    @Override
    public void setSize(long size) {
        getManifest().setSize(size);
    }

    @Override
    public URL getUrl() {
        return getManifest().getUrl();
    }

    @Override
    public String getFileName() {
        return getManifest().getFileName();
    }
}

package net.dirtcraft.dirtlauncher.data.Curse;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.Download;
import net.dirtcraft.dirtlauncher.game.installation.tasks.download.DownloadInfo;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    public CompletableFuture<CurseFile> getManifestAsync(Executor executor){
        return CompletableFuture.supplyAsync(this::getManifest, executor);
    }

    @SuppressWarnings("UnstableApiUsage")
    private CurseFile getManifest(){
        final TypeToken<CurseFile> type = new TypeToken<CurseFile>(){};
        return WebUtils.getGsonFromUrl(getDownloadUrl(), type).orElse(null);
    }

    public boolean equals(CurseMetaFileReference o){
        return projectID == o.projectID && fileID == o.fileID;
    }

    public boolean isRequired(){
        return required;
    }

    public DownloadInfo getDownloadInfo(Path folder) {
        return () -> {
            final CurseFile manifest = getManifest();
            final URL src = MiscUtils.getURL(manifest.downloadUrl.replaceAll("\\s", "%20")).orElse(null);
            final File dest = folder.resolve(manifest.fileName).toFile();
            return new Download(src, dest, manifest.fileLength);
        };
    }
}

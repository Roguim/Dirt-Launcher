package net.dirtcraft.dirtlauncher.game.installation.manifests.platforms;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlauncher.utils.WebUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseManifest {
    @Nonnull public final Minecraft minecraft;
    @Nonnull public final String manifestType;
    @Nonnull public final String manifestVersion;
    @Nonnull public final String name;
    @Nonnull public final String version;
    @Nonnull public final String author;
    @Nonnull public final List<CurseMetadataReference> files;

    public static class Minecraft{
        @Nonnull public final String version;
        @Nonnull public final List<ModLoader> modLoaders;

        @SuppressWarnings("ConstantConditions")
        private Minecraft(){
            version = null;
            modLoaders = null;
        }
    }

    public static class ModLoader{
        @Nonnull public final String id;
        public final boolean primary;

        @SuppressWarnings("ConstantConditions")
        private ModLoader(){
            id = null;
            primary = false;
        }
    }

    public static class CurseMetadataReference {
        public final long projectID;
        public final long fileID;
        public final boolean required;

        public String getUrl(){
            return String.format("https://addons-ecs.forgesvc.net/api/v2/addon/%s/file/%s", projectID, fileID);
        }

        public CompletableFuture<ModManifest> getManifestAsync(Executor executor){
            return CompletableFuture.supplyAsync(()->{
                @SuppressWarnings("UnstableApiUsage")
                TypeToken<ModManifest> token = new TypeToken<ModManifest>(){};
                return WebUtils.getGsonFromUrl(getUrl(), token).orElse(null);
            }, executor);
        }

        public boolean equals(CurseMetadataReference o){
            return projectID == o.projectID && fileID == o.fileID;
        }

        private CurseMetadataReference(){
            projectID = -1;
            fileID = -1;
            required = false;
        }
    }

    public static class ModManifest{
        public final long id;
        @Nonnull public final String displayName;
        @Nonnull public final String fileName;
        @Nonnull public final String downloadUrl;

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

        @SuppressWarnings("ConstantConditions")
        private ModManifest(){
            id = -1;
            displayName = null;
            fileName = null;
            downloadUrl = null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private CurseManifest(){
        minecraft = null;
        manifestType = null;
        manifestVersion = null;
        name = null;
        version = null;
        author = null;
        files = null;
    }
}

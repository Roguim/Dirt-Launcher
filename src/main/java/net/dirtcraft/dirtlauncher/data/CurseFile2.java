package net.dirtcraft.dirtlauncher.data;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CurseFile2 {
    private CurseFile2(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final long id;
    public final String displayName;
    public final String fileName;
    public final String downloadUrl;

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

}

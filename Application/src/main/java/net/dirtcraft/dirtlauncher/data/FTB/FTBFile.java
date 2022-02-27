package net.dirtcraft.dirtlauncher.data.FTB;

import net.dirtcraft.dirtlauncher.utils.WebUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FTBFile {
    private FTBFile(int i) throws InstantiationException {
        throw new InstantiationException("This is a data class intended to only be constructed by GSON.");
    }
    public final String version;
    public final String path;
    public final String url;
    public final String sha1;
    public final int size;
    public final List<String> tags;
    public final boolean clientonly;
    public final boolean serveronly;
    public final int id;
    public final String name;
    public final int updated;

    public CompletableFuture<Void> downloadAsync(File modsFolder, Executor executor){
        return CompletableFuture.runAsync(()-> download(modsFolder), executor);
    }

    private void download(File modsFolder){
        try{
            WebUtils.copyURLToFile(url.replaceAll("\\s", "%20"), new File(modsFolder, name));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

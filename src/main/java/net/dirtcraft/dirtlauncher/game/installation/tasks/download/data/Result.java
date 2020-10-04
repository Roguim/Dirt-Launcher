package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class Result {
    private final Throwable e;
    private final IDownload value;
    private Path folder;
    public Result(Throwable e, IDownload value, Path folder){
        this.e = e;
        this.value = value;
        this.folder = folder;
    }

    public boolean finishedExceptionally(){
        return e != null;
    }

    public Optional<Throwable> getException(){
        return Optional.ofNullable(e);
    }

    public File getFile(){
        return folder.resolve(value.getFileName()).toFile();
    }

    public String getFileName(){
        return value.getFileName();
    }

    public Path getFolder(){
        return folder;
    }

    public boolean relocate(Path folder){
        final File old = getFile();
        this.folder = folder;
        return old.renameTo(getFile());
    }
}

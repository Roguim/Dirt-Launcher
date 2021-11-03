package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.nio.file.Path;
import java.util.Optional;

public interface IFileDownload extends IDownload {
    default DownloadTask getDownload(){
        return new DownloadTask(this, getFolder());
    }

    Path getFolder();

    Optional<String> getSha1();

    boolean verify();
}

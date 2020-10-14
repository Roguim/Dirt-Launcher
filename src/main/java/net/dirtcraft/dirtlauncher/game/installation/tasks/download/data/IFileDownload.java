package net.dirtcraft.dirtlauncher.game.installation.tasks.download.data;

import java.nio.file.Path;

public interface IFileDownload extends IDownload {
    default DownloadTask getDownload(){
        return new DownloadTask(this, getFolder());
    }

    Path getFolder();
}

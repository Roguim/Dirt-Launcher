package net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java;

import net.dirtcraft.dirtlauncher.lib.data.json.mojang.FileDownload;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaVersionManifest {
    private JavaVersionManifest(int i) { throw new RuntimeException("you dun goof"); }
    private Map<String, DataFile> files;

    public List<DownloadTask> getDownloads(File folder) {
        List<DownloadTask> downloads = new ArrayList<>();
        files.forEach((path, archive)->{
            if (archive.downloads != null) {
                FileDownload dl = archive.downloads.get("raw");
                downloads.add(new DownloadTask(dl.getUrl(), new File(folder, path), dl.size, dl.sha1));
            }
        });
        return downloads;
    }

    private static class DataFile {
        private DataFile(int i) { throw new RuntimeException("you dun goof"); }
        public final String type;
        public final Map<String, FileDownload> downloads;
        public final boolean executable;
    }
}

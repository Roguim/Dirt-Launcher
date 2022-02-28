package net.dirtcraft.dirtlauncher.lib.data.json.dirtcraft;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class JavaFx {
    public static final JavaFx RUNTIME = new JavaFx();
    private static final String WIN_URL = "https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip";
    private static final String LINUX_URL = "https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_linux-x64_bin-jmods.zip";
    private static final String OSX_URL = "https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_osx-x64_bin-sdk.zip";
    public static final String[] MODULES = {"javafx.controls", "javafx.fxml", "javafx.web"};
    public Path folder;
    public File libs;

    private URL getDownload() {
        try {
            if (SystemUtils.IS_OS_MAC) return new URL(OSX_URL);
            if (SystemUtils.IS_OS_LINUX) return new URL(LINUX_URL);
            if (SystemUtils.IS_OS_WINDOWS) return new URL(WIN_URL);
            else throw new RuntimeException("Unsupported System!");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public DownloadTask getArchive(File dest) {
        return new DownloadTask(getDownload(), dest);
    }

    public Path getFolder() {
        return folder == null? folder = Constants.DIR_RUNTIMES.resolve("javafx-sdk-17.0.2") : folder;
    }

    public File getLibs() {
        return libs == null? libs = getFolder().resolve("lib").toFile() : libs;
    }

    public boolean isInstalled(){
        return getLibs().exists();
    }
}

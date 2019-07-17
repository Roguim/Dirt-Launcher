package net.dirtcraft.dirtlauncher.backend.config;

import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Directories {

    public static File getLauncherDirectory() {
        final Path fileEnding = Paths.get("DirtCraft", "DirtLauncher");

        // If it's windows, use AppData
        if (SystemUtils.IS_OS_WINDOWS) {
            return Paths.get(System.getenv("AppData")).resolve(fileEnding).toFile();
        }
        // If it's linux, use the user's Application Support directory
        else if (SystemUtils.IS_OS_MAC) {
            return Paths.get(System.getProperty("user.home") , "Library" , "Application Support").resolve(fileEnding).toFile();
        }
        // Otherwise, we can assume it's probably linux, so we'll use their application folder
        else {
            return Paths.get(System.getProperty("user.home")).resolve(fileEnding).toFile();
        }
    }

    public static File getGameDirectory() {

        JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());
        if (config != null && config.has("game-directory")){
            return new File(config.get("game-directory").getAsString());
        } else return getLauncherDirectory();
    }

    public static Path getLogDirectory() {
        return Paths.get(getGameDirectory().getPath(),"logs");
    }

    public static File getInstancesDirectory() {
        return Paths.get(getGameDirectory().getPath(), "instances").toFile();
    }

    public static File getVersionsDirectory() {
        return Paths.get(getGameDirectory().getPath(), "versions").toFile();
    }

    public static File getAssetsDirectory() {
        return Paths.get(getGameDirectory().getPath(), "assets").toFile();
    }

    public static File getForgeDirectory() {
        return Paths.get(getGameDirectory().getPath(), "forge").toFile();
    }

    public static File getDirectoryManifest(File directory) {
        return new File(directory.getPath(), "manifest.json");
    }
    public static File getConfiguration() {
        return new File(getLauncherDirectory().getPath(), "configuration.json");
    }

    public static File getLog(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        Date date = new Date();
        String fname = dateFormat.format(date);
        return new File(getLogDirectory().toFile(), fname+".log");
    }
}

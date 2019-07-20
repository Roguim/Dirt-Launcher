package net.dirtcraft.dirtlauncher.backend.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class SettingsManager {
    private int minimumRam;
    private int maximumRam;
    private String javaArguments;
    private Path gameDirectory;
    private final transient Path launcherDirectory;

    public SettingsManager(Path launcherDirectory){
        this.launcherDirectory = launcherDirectory;
        final JsonObject config = FileUtils.readaJsonFromFile(getConfiguration());
        if (getConfiguration().exists() && config != null){
            if (config.has("minimum-ram")) minimumRam = config.get("minimum-ram").getAsInt();
            else config.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
            if (config.has("maximum-ram")) maximumRam = config.get("maximum-ram").getAsInt();
            else config.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
            if (config.has("java-arguments")) javaArguments = config.get("java-arguments").getAsString();
            else config.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
            if (config.has("game-directory")) gameDirectory = Paths.get(config.get("game-directory").getAsString());
            else config.addProperty("game-directory", getLauncherDirectory().toString());
        } else {
            minimumRam = RamUtils.getMinimumRam() * 1024;
            maximumRam = RamUtils.getRecommendedRam() * 1024;
            javaArguments = Internal.DEFAULT_JAVA_ARGS;
            gameDirectory = launcherDirectory;
            initGameDirectory();
            saveSettings();
        }
    }

    public File getLog(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        Date date = new Date();
        String fname = dateFormat.format(date);
        return new File(getLogDirectory().toFile(), fname+".log");
    }

    public void initGameDirectory(){
        // Ensure that the application folders are created
        getGameDirectory().toFile().mkdirs();
        getInstancesDirectory().mkdirs();
        getVersionsDirectory().mkdirs();
        getAssetsDirectory().mkdirs();
        getForgeDirectory().mkdirs();
        if(!getDirectoryManifest(getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeaJsonToFile(getDirectoryManifest(getInstancesDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeaJsonToFile(getDirectoryManifest(getVersionsDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeaJsonToFile(getDirectoryManifest(getAssetsDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeaJsonToFile(getDirectoryManifest(getForgeDirectory()), emptyManifest);
        }
    }

    private void saveSettings(){
        final JsonObject config = new JsonObject();
        config.addProperty("minimum-ram", minimumRam);
        config.addProperty("maximum-ram", maximumRam);
        config.addProperty("java-arguments", javaArguments);
        config.addProperty("game-directory", gameDirectory.toString());
        FileUtils.writeaJsonToFile(getConfiguration(), config);
    }

    public void updateSettings(int minimumRam, int maximumRam, String javaArguments, String gameDirectory){
        this.minimumRam = minimumRam;
        this.maximumRam = maximumRam;
        this.javaArguments = javaArguments;
        this.gameDirectory = Paths.get(gameDirectory);
        saveSettings();
    }

    public Path getLauncherDirectory() {
        return launcherDirectory;
    }

    public Path getLogDirectory() {
        return launcherDirectory.resolve("logs");
    }

    public File getInstancesDirectory() {
        return gameDirectory.resolve("instances").toFile();
    }

    public File getVersionsDirectory() {
        return gameDirectory.resolve("versions").toFile();
    }

    public File getAssetsDirectory() {
        return gameDirectory.resolve("assets").toFile();
    }

    public File getForgeDirectory() {
        return gameDirectory.resolve("forge").toFile();
    }

    public File getDirectoryManifest(File directory) {
        return gameDirectory.resolve("manifest.json").toFile();
    }

    public File getConfiguration() {
        return launcherDirectory.resolve("configuration.json").toFile();
    }

    public int getMinimumRam() {
        return minimumRam;
    }

    public int getMaximumRam() {
        return maximumRam;
    }

    public String getJavaArguments() {
        return javaArguments;
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }
}

package net.dirtcraft.dirtlauncher.backend.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.management.OperatingSystemMXBean;
import net.dirtcraft.dirtlauncher.Main;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class Config {
    private int minimumRam;
    private int maximumRam;
    private String javaArguments;
    private Path gameDirectory;
    private final transient Path launcherDirectory;

    public Config(Path launcherDirectory){
        File configFile = launcherDirectory.resolve("configuration.json").toFile();
        this.launcherDirectory = launcherDirectory;
        JsonObject config;
        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            config = parser.parse(reader).getAsJsonObject();
        } catch (IOException e){
            config = null;
        }
        if (configFile.exists() && config != null){
            if (config.has("minimum-ram")) minimumRam = config.get("minimum-ram").getAsInt();
            else config.addProperty("minimum-ram", getDefaultMinimumRam() * 1024);
            if (config.has("maximum-ram")) maximumRam = config.get("maximum-ram").getAsInt();
            else config.addProperty("maximum-ram", getDefaultRecommendedRam() * 1024);
            if (config.has("java-arguments")) javaArguments = config.get("java-arguments").getAsString();
            else config.addProperty("java-arguments", Constants.DEFAULT_JAVA_ARGS);
            if (config.has("game-directory")) gameDirectory = Paths.get(config.get("game-directory").getAsString());
            else config.addProperty("game-directory", launcherDirectory.toString());
        } else {
            minimumRam = getDefaultMinimumRam() * 1024;
            maximumRam = getDefaultRecommendedRam() * 1024;
            javaArguments = Constants.DEFAULT_JAVA_ARGS;
            gameDirectory = launcherDirectory;
            initGameDirectory();
            saveSettings();
        }
    }

    public int getDefaultRecommendedRam() {
        final long maxMemory = (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024;
        if (maxMemory > 10000) return 8;
        else if (maxMemory > 7000) return 6;
        else if (maxMemory > 5000) return 4;
        else if (maxMemory > 2000) return 3;
        else return 2;
    }

    public int getDefaultMinimumRam() {
        final long maxMemory = (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024;
        if (maxMemory > 5000) return 4;
        else if (maxMemory > 3000) return 3;
        else return 2;
    }

    private void initGameDirectory(){
        final Logger logger = Main.getLogger();
        logger.info(getGameDirectory().toFile().mkdirs()?"Successfully created":"Failed to create"+" game directory");
        logger.info(getInstancesDirectory().mkdirs()?"Successfully created":"Failed to create"+" instances directory");
        logger.info(getVersionsDirectory().mkdirs()?"Successfully created":"Failed to create"+" versions directory");
        logger.info(getAssetsDirectory().mkdirs()?"Successfully created":"Failed to create"+" assets directory.");
        logger.info(getForgeDirectory().mkdirs()?"Successfully created":"Failed to create"+" forge directory.");
        // Ensure that the application folders are created
        if(!getDirectoryManifest(getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeJsonToFile(getDirectoryManifest(getInstancesDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeJsonToFile(getDirectoryManifest(getVersionsDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeJsonToFile(getDirectoryManifest(getAssetsDirectory()), emptyManifest);
        }
        if(!getDirectoryManifest(getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeJsonToFile(getDirectoryManifest(getForgeDirectory()), emptyManifest);
        }
    }

    private void saveSettings(){
        File configFile = launcherDirectory.resolve("configuration.json").toFile();
        final JsonObject config = new JsonObject();
        config.addProperty("minimum-ram", minimumRam);
        config.addProperty("maximum-ram", maximumRam);
        config.addProperty("java-arguments", javaArguments);
        config.addProperty("game-directory", gameDirectory.toString());
        FileUtils.writeJsonToFile(configFile, config);
    }

    public void updateSettings(int minimumRam, int maximumRam, String javaArguments, String gameDirectory){
        final boolean changedDir = !this.gameDirectory.toString().equals(gameDirectory);
        final File oldGameDir = getGameDirectory().toFile();
        final File oldInstanceDir = getInstancesDirectory();
        final File oldVersionDir = getVersionsDirectory();
        final File oldAssetsDir = getAssetsDirectory();
        final File oldForgeDir = getForgeDirectory();
        this.minimumRam = minimumRam;
        this.maximumRam = maximumRam;
        this.javaArguments = javaArguments;
        this.gameDirectory = Paths.get(gameDirectory);
        if (changedDir){
            try {
                org.apache.commons.io.FileUtils.moveDirectory(oldInstanceDir, getInstancesDirectory());
                org.apache.commons.io.FileUtils.moveDirectory(oldVersionDir, getVersionsDirectory());
                org.apache.commons.io.FileUtils.moveDirectory(oldAssetsDir, getAssetsDirectory());
                org.apache.commons.io.FileUtils.moveDirectory(oldForgeDir, getForgeDirectory());
            } catch (IOException e){
                Main.getLogger().error(e);
            }
            Main.getLogger().info(oldGameDir.delete()?"Successfully deleted":"Failed to delete"+" "+oldGameDir);
            initGameDirectory();
        }
        saveSettings();
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
        return directory.toPath().resolve("manifest.json").toFile();
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

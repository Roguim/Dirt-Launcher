package net.dirtcraft.dirtlauncher.backend.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public final class SettingsManager {
    private int minimumRam;
    private int maximumRam;
    private String javaArguments;
    private Path gameDirectory;
    private final transient Path launcherDirectory;

    public SettingsManager(Path launcherDirectory){
        this.launcherDirectory = launcherDirectory;
        JsonObject config;
        try (FileReader reader = new FileReader(getConfiguration())) {
            JsonParser parser = new JsonParser();
            config = parser.parse(reader).getAsJsonObject();
        } catch (IOException e){
            config = null;
        }
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
        getGameDirectory().toFile().mkdirs();
        getInstancesDirectory().mkdirs();
        getVersionsDirectory().mkdirs();
        getAssetsDirectory().mkdirs();
        getForgeDirectory().mkdirs();
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
        final JsonObject config = new JsonObject();
        config.addProperty("minimum-ram", minimumRam);
        config.addProperty("maximum-ram", maximumRam);
        config.addProperty("java-arguments", javaArguments);
        config.addProperty("game-directory", gameDirectory.toString());
        FileUtils.writeJsonToFile(getConfiguration(), config);
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
            oldGameDir.delete();
            initGameDirectory();
        }
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
        return directory.toPath().resolve("manifest.json").toFile();
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

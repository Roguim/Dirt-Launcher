package net.dirtcraft.dirtlauncher.configuration;

import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import net.dirtcraft.dirtlauncher.logging.Logger;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings {
    private final int minimumRam;
    private final int maximumRam;
    private final String javaArguments;
    private final String  gameDirectory;
    transient private Path gameDirectoryPath;
    transient public boolean migrated;

    public Settings(int ramMin, int ramMax, String javaArguments, String gameDirectory){
        this.minimumRam = ramMin;
        this.maximumRam = ramMax;
        this.javaArguments = javaArguments;
        this.gameDirectory = gameDirectory;
        this.gameDirectoryPath = Paths.get(gameDirectory);
    }
    public Settings(Path launchDir){
        minimumRam = getDefaultMinimumRam() * 1024;
        maximumRam = getDefaultRecommendedRam() * 1024;
        javaArguments = Constants.DEFAULT_JAVA_ARGS;
        gameDirectory = launchDir.toString();
        gameDirectoryPath = launchDir;
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
        if (gameDirectoryPath != null) return gameDirectoryPath;
        else return (gameDirectoryPath = Paths.get(gameDirectory));
    }

    public String getGameDirectoryAsString() {
        return gameDirectory;
    }

    public boolean isValid(){
        return maximumRam > 0
                && minimumRam > 0
                && javaArguments != null
                && gameDirectory != null;
    }

    public Path getInstancesDirectory() {
        return getGameDirectory().resolve("instances");
    }

    public Path getVersionsDirectory() {
        return getGameDirectory().resolve("versions");
    }

    public Path getAssetsDirectory() {
        return getGameDirectory().resolve("assets");
    }

    public Path getForgeDirectory() {
        return getGameDirectory().resolve("forge");
    }

    public Path getLogDirectory() {
        return getGameDirectory().resolve("logs");
    }

    private int getDefaultRecommendedRam() {
        final long maxMemory = (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024;
        if (maxMemory > 10000) return 8;
        else if (maxMemory > 7000) return 6;
        else if (maxMemory > 5000) return 4;
        else if (maxMemory > 2000) return 3;
        else return 2;
    }

    private int getDefaultMinimumRam() {
        final long maxMemory = (((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024;
        if (maxMemory > 5000) return 4;
        else if (maxMemory > 3000) return 3;
        else return 2;
    }

    public static Settings migrate(JsonObject jsonObject){
        try{
            int minimumRam = jsonObject.get("minimum-ram").getAsInt();
            int maximumRam = jsonObject.get("maximum-ram").getAsInt();
            String javaArguments = jsonObject.get("java-arguments").getAsString();
            String gameDirectory = jsonObject.get("game-directory").getAsString();
            Settings settings = new Settings(minimumRam, maximumRam, javaArguments, gameDirectory);
            settings.migrated = true;
            return settings;
        } catch (Exception e){
            Logger.INSTANCE.error(e);
            return null;
        }
    }
}

package net.dirtcraft.dirtlauncher.data.Minecraft;

import net.dirtcraft.dirtlauncher.game.installation.tasks.download.data.IDownload;

import java.util.List;
import java.util.Map;

public class GameVersion {
    private Download assetIndex;
    private String assets;
    private Map<String, Download> downloads;
    private JavaVersion javaVersion;
    private String id;
    private List<Library> libraries;
    //private Map<String, ?> logging;
    private String mainClass;
    private String minecraftArguments;
    private String minimumLauncherVersion;
    private String releaseTime;
    private String time;
    private String type;

    public JavaVersion getJava() {
        return javaVersion != null? javaVersion : JavaVersion.legacy;
    }

    public Download getAssetIndex() {
        return assetIndex;
    }

    public String getAssets() {
        return assets;
    }

    public Map<String, Download> getDownloads() {
        return downloads;
    }

    public String getId() {
        return id;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public String getType() {
        return type;
    }

    public IDownload getDownload(String key) {
        return downloads.get(key);
    }
}

package net.dirtcraft.dirtlauncher.lib.data.json.mojang;

import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java.JavaVersion;

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
        return javaVersion != null? javaVersion : JavaVersion.LEGACY;
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
}

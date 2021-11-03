package net.dirtcraft.dirtlauncher.data.Minecraft;

import java.io.File;

public class JavaVersion {
    public static final JavaVersion legacy = new JavaVersion("jre-legacy", 8);
    public final String component;
    public final int majorVersion;
    private JavaVersion(String component, int majorVersion){
        this.component = component;
        this.majorVersion = majorVersion;
    }

    public String getFolder(){
        return String.format("%s_(%d)", component, majorVersion);
    }

    public boolean isInstalled(File folder) {
        return new File(folder, getFolder()).exists();
    }
}

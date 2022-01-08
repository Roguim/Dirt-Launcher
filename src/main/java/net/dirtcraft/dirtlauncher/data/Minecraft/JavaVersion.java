package net.dirtcraft.dirtlauncher.data.Minecraft;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaVersion {
    public static final JavaVersion legacy = new JavaVersion("jre-legacy", 8);
    public final String component;
    public final int majorVersion;
    public JavaVersion(String component, int majorVersion){
        this.component = component;
        this.majorVersion = majorVersion;
    }

    public String getFolder(){
        return String.format("%s (%d)", component, majorVersion);
    }

    public Path getJavaWExec() {
        return Paths.get(String.format("%s (%d)", component, majorVersion), "bin", "javaw");
    }

    public Path getJavaExec() {
        return Paths.get(String.format("%s (%d)", component, majorVersion), "bin", "java");
    }

    public boolean isInstalled(File folder) {
        return new File(folder, getFolder()).exists();
    }
}

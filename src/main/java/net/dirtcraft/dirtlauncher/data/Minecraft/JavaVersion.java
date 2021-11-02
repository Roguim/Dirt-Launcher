package net.dirtcraft.dirtlauncher.data.Minecraft;

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
}

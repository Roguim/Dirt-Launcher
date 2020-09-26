package net.dirtcraft.dirtlauncher.data.Minecraft;

import org.apache.commons.lang3.SystemUtils;

public class Rule {
    public Rule(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    public final String action;
    public final OperatingSystem targetOS;

    public boolean canDownload(){
        final boolean shouldDownload = action.equalsIgnoreCase("allow");
        if (targetOS == null) return shouldDownload;
        return shouldDownload == targetOS.matches();
    }

    public static class OperatingSystem {
        public OperatingSystem(int i) throws InstantiationException{
            throw new InstantiationException("Gson data class. Not to be manually created.");
        }
        public final String name;

        public boolean matches(){
            switch (name.toLowerCase()){
                case "osx": return SystemUtils.IS_OS_MAC;
                case "linux": return SystemUtils.IS_OS_LINUX;
                case "windows": return SystemUtils.IS_OS_WINDOWS;
                default: return false;
            }
        }
    }

}

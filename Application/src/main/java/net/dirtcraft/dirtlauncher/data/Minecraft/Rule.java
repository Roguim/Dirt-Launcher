package net.dirtcraft.dirtlauncher.data.Minecraft;

import org.apache.commons.lang3.SystemUtils;

public class Rule {
    public Rule(int i) throws InstantiationException{
        throw new InstantiationException("Gson data class. Not to be manually created.");
    }
    public final String action;
    public final OperatingSystem os; // 1.16.5
    public final OperatingSystem targetOS; //1.12.2

    public boolean canDownload(){
        final boolean shouldDownload = action.equalsIgnoreCase("allow");
        if (os != null) return shouldDownload == os.matches();
        else if (targetOS != null) return shouldDownload == targetOS.matches();
        else return shouldDownload;
    }

    public static boolean matches(String os){
        switch (os.toLowerCase()){
            case "osx": return SystemUtils.IS_OS_MAC;
            case "linux": return SystemUtils.IS_OS_LINUX;
            case "windows": return SystemUtils.IS_OS_WINDOWS;
            default: return false;
        }
    }

    public static class OperatingSystem {
        public OperatingSystem(int i) throws InstantiationException{
            throw new InstantiationException("Gson data class. Not to be manually created.");
        }
        public final String name;

        public boolean matches(){
            return Rule.matches(name);
        }
    }

}

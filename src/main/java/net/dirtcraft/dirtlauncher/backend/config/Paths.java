package net.dirtcraft.dirtlauncher.backend.config;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class Paths {

    public static File getInstallDirectory() {
        String fileEnding = File.separator + "DirtCraft" + File.separator + "DirtLauncher";
        // If it's windows, use AppData
        if(SystemUtils.IS_OS_WINDOWS) {
            return new File(System.getenv("AppData") + fileEnding);
        }
        // If it's linux, use the user's home directory
        else if(SystemUtils.IS_OS_LINUX) {
            return new File(System.getProperty("user.home") + fileEnding);
        }
        // Otherwise, we can assume it's probably mac, so we'll use their application folder
        else {
            return new File(System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + fileEnding);
        }
    }
}

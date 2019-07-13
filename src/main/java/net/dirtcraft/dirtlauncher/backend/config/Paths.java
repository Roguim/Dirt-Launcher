package net.dirtcraft.dirtlauncher.backend.config;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class Paths {

    public static File getInstallDirectory() {
        final String fileEnding = File.separator + "DirtCraft" + File.separator + "DirtLauncher";
        // If it's windows, use AppData
        if (SystemUtils.IS_OS_WINDOWS) {
            return new File(System.getenv("AppData") + fileEnding);
        }
        // If it's linux, use the user's Application Support directory
        else if (SystemUtils.IS_OS_MAC) {
            return new File(System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + fileEnding);
        }
        // Otherwise, we can assume it's probably linux, so we'll use their application folder
        else {
            return new File(System.getProperty("user.home") + fileEnding);
        }
    }

    public static File getInstancesDirectory() {
        return new File(getInstallDirectory().getPath() + File.separator + "instances");
    }

    public static File getVersionsDirectory() {
        return new File(getInstallDirectory().getPath() + File.separator + "versions");
    }

    public static File getAssetsDirectory() {
        return new File(getInstallDirectory().getPath() + File.separator + "assets");
    }

    public static File getForgeDirectory() {
        return new File(getInstallDirectory().getPath() + File.separator + "forge");
    }

    public static File getDirectoryManifest(File directory) {
        return new File(directory.getPath() + File.separator + "manifest.json");
    }

    public static File getConfiguration() {
        return new File(getInstallDirectory().getPath() + File.separator + "configuration.json");
    }
}

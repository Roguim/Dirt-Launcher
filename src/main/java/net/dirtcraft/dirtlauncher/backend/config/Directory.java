package net.dirtcraft.dirtlauncher.backend.config;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directory {

    public static File getInstallDirectory() {
        final Path fileEnding = Paths.get("DirtCraft", "DirtLauncher");

        // If it's windows, use AppData
        if (SystemUtils.IS_OS_WINDOWS) {
            return Paths.get(System.getenv("AppData")).resolve(fileEnding).toFile();
        }
        // If it's linux, use the user's Application Support directory
        else if (SystemUtils.IS_OS_MAC) {
            return Paths.get(System.getProperty("user.home") , "Library" , "Application Support").resolve(fileEnding).toFile();
        }
        // Otherwise, we can assume it's probably linux, so we'll use their application folder
        else {
            return Paths.get(System.getProperty("user.home")).resolve(fileEnding).toFile();
        }
    }

    public static File getInstancesDirectory() {
        return Paths.get(getInstallDirectory().getPath(), "instances").toFile();
    }

    public static File getVersionsDirectory() {
        return Paths.get(getInstallDirectory().getPath(), "versions").toFile();
    }

    public static File getAssetsDirectory() {
        return Paths.get(getInstallDirectory().getPath(), "assets").toFile();
    }

    public static File getForgeDirectory() {
        return Paths.get(getInstallDirectory().getPath(), "forge").toFile();
    }

    public static File getDirectoryManifest(File directory) {
        return Paths.get(directory.getPath(), "manifest.json").toFile();
    }

    public static File getConfiguration() {
        return Paths.get(getInstallDirectory().getPath(), "configuration.json").toFile();
    }
}

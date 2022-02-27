package net.dirtcraft.dirtlauncher.lib;

import net.dirtcraft.dirtlauncher.lib.config.Constants;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirtLib {
    public static Path LAUNCHER_DIR = DirtLib.getLauncherDirectory();
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    static {
        CompletableFuture.runAsync(()->{
            if (Constants.DIR_TEMP.toFile().exists()) for (File f : Constants.DIR_TEMP.toFile().listFiles()) f.delete();
        }, THREAD_POOL);
    }

    private static Path getLauncherDirectory(){
        if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            return Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else return Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
    }
}

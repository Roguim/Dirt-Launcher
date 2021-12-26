package net.dirtcraft.dirtlauncher;

import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final String DEFAULT_JRE = "jre8_x64";
    public static final String DEFAULT_JFX = "javafx-sdk-17.0.1";
    public static void main(String[] args) throws IOException {
        Path launcherDirectory = getLauncherDirectory();
        Path jre = launcherDirectory
                .resolve("Runtime")
                .resolve(DEFAULT_JRE)
                .resolve("bin")
                .resolve("javaw.exe");
        Path jfx = launcherDirectory
                .resolve("Runtime")
                .resolve(DEFAULT_JFX)
                .resolve("lib");
        String exec = String.format("%s --module-path %s --add-modules javafx.controls,javafx.fxml,javafx.web -cp Dirt-Launcher.jar net.dirtcraft.dirtlauncher.DirtLauncher -installed -x64", jre.toString(), jfx.toString());
        System.out.println("RELAUNCHING");
        Runtime.getRuntime().exec(exec);
    }

    private static Path getLauncherDirectory(){
        if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            return Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else return Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
        //Logger.INSTANCE.debug("Block Start @ " + (System.currentTimeMillis() - x) + "ms");
    }
}

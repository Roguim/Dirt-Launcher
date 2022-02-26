package net.dirtcraft.dirtlauncher;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dirtcraft.dirtlauncher.data.serializers.MultiMapAdapter;
import net.dirtcraft.dirtlauncher.utils.forge.Artifact;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static ThreadPoolExecutor THREAD_POOL = (ThreadPoolExecutor) Executors.newFixedThreadPool(24);
    public static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Artifact.class, new Artifact.Adapter())
            .registerTypeAdapter(Multimap.class, new MultiMapAdapter<>())
            .create();
    public static final String DEFAULT_JRE = "default_jre";
    public static final String DEFAULT_JFX = "default_jfx";
    public static final String[] MODULES = {
            "javafx.controls",
            "javafx.fxml",
            "javafx.web"
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        Path launcherDirectory = getLauncherDirectory();
        Path jre = launcherDirectory
                .resolve("Runtime")
                .resolve(DEFAULT_JRE)
                .resolve("bin")
                .resolve("java");
        Path jfx = launcherDirectory
                .resolve("Runtime")
                .resolve(DEFAULT_JFX)
                .resolve("lib");
        System.out.println("RELAUNCHING");
        Process p = new ProcessBuilder(
                jre.toString(),
                "-Dprism.verbose=true",
                "--module-path",
                jfx.toString(),
                "--add-modules",
                String.join(",", MODULES),
                "-cp",
                "Dirt-Launcher.jar",
                "net.dirtcraft.dirtlauncher.DirtLauncher",
                "-debug",
                "-verbose"
        ).inheritIO().start();
        while (p.isAlive()) Thread.sleep(5000);

    }

    private static Path getLauncherDirectory(){
        if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            return Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else return Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
    }
}

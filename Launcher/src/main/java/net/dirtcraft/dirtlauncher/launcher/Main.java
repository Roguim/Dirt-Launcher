package net.dirtcraft.dirtlauncher.launcher;

import net.dirtcraft.dirtlauncher.lib.DirtLib;
import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.json.dirtcraft.Version;
import net.dirtcraft.dirtlauncher.lib.data.json.dirtcraft.JavaFx;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java.JavaVersion;
import net.dirtcraft.dirtlauncher.lib.data.tasks.CopyTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.ExtractTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.TextRenderers;
import net.dirtcraft.dirtlauncher.lib.parsing.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class Main extends DirtLib{

    public static void main(String[] args) throws IOException {
        installJava();
        installJfx();
        checkRelease();
        launchApp(getJavaPath(),
                "-Dprism.verbose=true",
                "--module-path",
                getJfxPath(),
                "--add-modules",
                getModules(),
                "-cp",
                "Dirt-Launcher.jar",
                "net.dirtcraft.dirtlauncher.DirtLauncher",
                "-debug",
                "-verbose"
        );
    }

    public static void launchApp(String... args) throws IOException {
        for (String arg : args) System.out.println(arg);
        new ProcessBuilder(args).directory(DirtLib.LAUNCHER_DIR.toFile()).inheritIO().start();
    }

    private static void checkRelease() throws IOException {
        try(InputStream current = Main.class.getResourceAsStream("/release.json")){
            File installed = LAUNCHER_DIR.resolve("Dirt-Launcher.jar").toFile();
            File executing = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!installed.exists() && !executing.isDirectory()) performJarUpdate(executing, installed);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private static String getModules(){
        return String.join(",", JavaFx.MODULES);
    }

    private static void installJfx() throws IOException {
        if (JavaFx.RUNTIME.isInstalled()) return;
        System.out.print("Fetching JavaFx Library!\n");
        DownloadTask task = JavaFx.RUNTIME.getArchive(Constants.DIR_TEMP.resolve("javafx.zip").toFile());
        TaskExecutor.prepare(Collections.singleton(task), TextRenderers.PROGRESS);
        TaskExecutor.execute(Collections.singleton(task), TextRenderers.BITRATE);
        System.out.print("\r\nDownload Complete!\n");
        System.out.print("Extracting Archive!\n");
        Collection<ExtractTask> tasks = ExtractTask.from(new ZipFile(task.destination), Constants.DIR_RUNTIMES);
        TaskExecutor.execute(tasks, TextRenderers.BITRATE);
        System.out.print("\r\nExtraction Complete!\n");
    }

    private static String getJfxPath() {
        return JavaFx.RUNTIME.getLibs().getPath();
    }

    private static void installJava(){
        if (JavaVersion.DEFAULT.isInstalled()) return;
        System.out.print("Fetching Default Java Runtime!\n");
        TaskExecutor.execute(JavaVersion.DEFAULT.getDownloads(), TextRenderers.BITRATE);
        System.out.print("\r\nDownload Complete!\n");
    }

    private static String getJavaPath() {
        return JavaVersion.DEFAULT.getJavaExec().getPath();
    }

    private static void performJarUpdate(File srcJar, File destJar) throws IOException {
            if (!srcJar.exists() || srcJar.isDirectory()) return;
            DownloadTask task = new DownloadTask(new URL("http://164.132.201.67/launcher/Dirt-Launcher.jar"), destJar);
            System.out.print("Downloading Latest JAR.\n");
            TaskExecutor.execute(Collections.singleton(task), TextRenderers.BITRATE);
            System.out.print("\r\nDownload Complete!\n");
    }
}

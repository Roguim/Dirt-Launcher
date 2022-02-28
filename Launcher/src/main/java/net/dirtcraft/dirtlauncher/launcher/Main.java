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
                "--illegal-access=permit",
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
        new ProcessBuilder(args).directory(DirtLib.LAUNCHER_DIR.toFile()).inheritIO().start();
    }

    private static void checkRelease() throws IOException {
        try(InputStream current = Main.class.getResourceAsStream("/release.json")){
            File installed = LAUNCHER_DIR.resolve("Dirt-Launcher.jar").toFile();
            File executing = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (executing.isDirectory() || (installed.exists() && installed.equals(executing))) return; //for now do not support updates here as the jar could be being ran here.
            Version installedRelease = JsonUtils.parseJson(new JarFile(installed), "release.json", Version.class);
            String installedVersion = installedRelease == null? "null" : installedRelease.version;
            Version executingRelease = JsonUtils.parseJson(current, Version.class);
            String executingVersion = executingRelease == null? "null" : executingRelease.version;
            Version remoteRelease = JsonUtils.parseJson(new URL("http://164.132.201.67/launcher/version.json"), Version.class);
            String remoteVersion = remoteRelease == null? "null" : remoteRelease.version;
            if (isLeftGreater(executingVersion, remoteVersion)){
                if (isLeftGreater(executingVersion, installedVersion)) performJarUpdate(executing,installed);
            } else {
                if (isLeftGreater(remoteVersion, installedVersion)) performWebUpdate(installed);
            }
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

    private static void performWebUpdate(File destJar) throws IOException{
        System.out.print("Fetching Update!\n");
        DownloadTask task = new DownloadTask(new URL("http://164.132.201.67/launcher/Dirt-Launcher.jar"), destJar);
        TaskExecutor.execute(Collections.singleton(task), TextRenderers.BITRATE);
        System.out.print("\r\nUpdate Complete!\n");

    }

    private static void performJarUpdate(File srcJar, File destJar) throws IOException {
            if (!srcJar.exists() || srcJar.isDirectory()) return;
            System.out.print("Executed jar contains new version! Applying update!!\n");
            CopyTask task = new CopyTask(srcJar, destJar);
            TaskExecutor.execute(Collections.singleton(task), TextRenderers.BITRATE);
            System.out.print("\r\nUpdate Complete!\n");
    }

    private static boolean isLeftGreater(String a, String b) {
        String[] specA = a.split("\\.");
        String[] specB = b.split("\\.");
        int len = Math.min(specA.length, specB.length);
        for (int i = 0; i < len; i++) {
            int partA = specA[i].matches("\\d+")? Integer.parseInt(specA[i]) : -1;
            int partB = specB[i].matches("\\d+")? Integer.parseInt(specB[i]) : -1;
            if (partA != partB) return partA > partB;
        }
        return false;
    }
}

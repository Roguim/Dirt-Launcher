package net.dirtcraft.dirtlauncher.launcher;

import net.dirtcraft.dirtlauncher.lib.DirtLib;
import net.dirtcraft.dirtlauncher.lib.config.Constants;
import net.dirtcraft.dirtlauncher.lib.data.json.dirtlauncher.JavaFx;
import net.dirtcraft.dirtlauncher.lib.data.json.mojang.Java.JavaVersion;
import net.dirtcraft.dirtlauncher.lib.data.tasks.CopyTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.DownloadTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.ExtractTask;
import net.dirtcraft.dirtlauncher.lib.data.tasks.TaskExecutor;
import net.dirtcraft.dirtlauncher.lib.data.tasks.renderers.TextRenderer;
import net.dirtcraft.dirtlauncher.lib.parsing.JsonUtils;
import net.dirtcraft.dirtlauncher.lib.util.Util;

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
        for (String s : args) System.out.println(s);
        Process p = new ProcessBuilder(args)
                .directory(DirtLib.LAUNCHER_DIR.toFile())
                .inheritIO()
                .start();
        while (p.isAlive()) Util.spin(5000);
    }

    private static void checkRelease() throws IOException {
        try(InputStream executing = Main.class.getResourceAsStream("/release.json")){
            File installed = LAUNCHER_DIR.resolve("Dirt-Launcher.jar").toFile();
            if (installed.exists()) return; //for now do not support updates here as the jar could be being ran here.
            Release installedRelease = JsonUtils.parseJson(new JarFile(installed), "release.json", Release.class);
            String installedVersion = installedRelease == null? "null" : installedRelease.version;
            Release executingRelease = JsonUtils.parseJson(executing, Release.class);
            String executingVersion = executingRelease == null? "null" : executingRelease.version;
            Release remoteRelease = JsonUtils.parseJson(new URL("http://164.132.201.67/launcher/version.json"), Release.class);
            String remoteVersion = remoteRelease == null? "null" : remoteRelease.version;
            if (isLeftGreater(executingVersion, remoteVersion)){
                if (isLeftGreater(executingVersion, installedVersion)) performJarUpdate();
            } else {
                if (isLeftGreater(remoteVersion, installedVersion)) performWebUpdate();
            }
        }
    }

    private static String getModules(){
        return String.join(",", JavaFx.MODULES);
    }

    private static void installJfx() throws IOException {
        if (JavaFx.RUNTIME.isInstalled()) return;
        TextRenderer textRenderer = new TextRenderer();
        System.out.print("Fetching JavaFx Library!\n");
        DownloadTask task = JavaFx.RUNTIME.getArchive(Constants.DIR_TEMP.resolve("javafx.zip").toFile());
        TaskExecutor.prepare(Collections.singleton(task), textRenderer, 20);
        TaskExecutor.execute(Collections.singleton(task), textRenderer, 20, "Downloading");
        System.out.print("\r\nDownload Complete!\n");
        System.out.print("Extracting Archive!\n");
        Collection<ExtractTask> tasks = ExtractTask.from(new ZipFile(task.destination), Constants.DIR_RUNTIMES);
        TaskExecutor.execute(tasks, textRenderer, 20, "Extracting");
        System.out.print("\r\nExtraction Complete!\n");
    }

    private static String getJfxPath() {
        return JavaFx.RUNTIME.getLibs().getPath();
    }

    private static void installJava(){
        if (JavaVersion.DEFAULT.isInstalled()) return;
        TextRenderer textRenderer = new TextRenderer();
        System.out.print("Fetching Default Java Runtime!\n");
        TaskExecutor.execute(JavaVersion.DEFAULT.getDownloads(), textRenderer, 20, "Downloading");
        System.out.print("\r\nDownload Complete!\n");
    }

    private static String getJavaPath() {
        return JavaVersion.DEFAULT.getJavaExec().getPath();
    }

    private static void performWebUpdate() throws IOException{
        TextRenderer textRenderer = new TextRenderer();
        System.out.print("Fetching Update!\n");
        DownloadTask task = new DownloadTask(new URL("http://164.132.201.67/launcher/Dirt-Launcher.jar"), LAUNCHER_DIR.resolve("Dirt-Launcher.jar").toFile());
        TaskExecutor.execute(Collections.singleton(task), textRenderer, 20, "Downloading");
        System.out.print("\r\nUpdate Complete!\n");

    }

    private static void performJarUpdate() throws IOException {
        try {
            File jar = new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!jar.exists() || jar.isDirectory()) return;
            TextRenderer textRenderer = new TextRenderer();
            System.out.print("Executed jar contains new version! Applying update!!\n");
            CopyTask task = new CopyTask(jar, LAUNCHER_DIR.resolve("Dirt-Launcher.jar").toFile());
            TaskExecutor.execute(Collections.singleton(task), textRenderer, 20, "Copying");
            System.out.print("\r\nUpdate Complete!\n");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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

    public static class Release {
        private String version = "${version}";
    }
}

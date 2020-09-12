package net.dirtcraft.dirtlauncher.game;

import com.google.gson.JsonElement;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.components.SystemTray;
import net.dirtcraft.dirtlauncher.gui.dialog.ErrorWindow;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.game.serverlist.Listing;
import net.dirtcraft.dirtlauncher.game.serverlist.ServerList;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchGame {

    public static boolean isGameRunning = false;

    public static void loadServerList(Modpack pack) {
        List<Listing> servers = ServerList.getCurrent(pack.getName());
        ServerList serverList = ServerList.builder(pack.getName());
        pack.getListings().ifPresent(listings -> {
            for (Listing listing : listings) {
                serverList.addServer(listing.getIp(), listing.getName());
            }
        });
        if (!pack.isPixelmon()) serverList.addServer((pack.getCode() + ".DIRTCRAFT.GG").toUpperCase(), "§c§lDirtCraft §8- §d" + pack.getName());
        serverList.addServer(("DIRTCRAFT.GG").toUpperCase(), "§c§lDirtCraft §8- §dHub");
        servers.forEach(serverList::addServer);
        serverList.build();
    }

    public static void launchPack(Modpack pack, Account session) {
        Config settings = Main.getConfig();
        final File instanceDirectory = new File(settings.getInstancesDirectory().getPath() + File.separator + pack.getFormattedName());

        List<String> args = new ArrayList<>();
        args.add(settings.getDefaultRuntime());

        // RAM
        args.add("-Xms" + settings.getMinimumRam() + "M");
        args.add("-Xmx" + settings.getMaximumRam() + "M");

        // Configurable Java Arguments
        String javaArgs = settings.getJavaArguments();
        if (MiscUtils.isEmptyOrNull(javaArgs)) args.addAll(Arrays.asList(Constants.DEFAULT_JAVA_ARGS.split(" ")));
        else args.addAll(Arrays.asList(javaArgs.split(" ")));

        // Language Tricks
        args.add("-Dfml.ignorePatchDiscrepancies=true");
        args.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        args.add("-Duser.language=en");
        args.add("-Duser.country=US");

        // Mojang Tricks
        args.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");

        // Natives path
        String nativesPath = settings.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + "natives";
        args.add("-Djava.library.path=" + nativesPath);
        args.add("-Dorg.lwjgl.librarypath=" + nativesPath);
        args.add("-Dnet.java.games.input.librarypath=" + nativesPath);
        args.add("-Duser.home=" + instanceDirectory.getPath());

        // Classpath
        args.add("-cp");
        args.add(getLibs(pack));

        //Loader class
        args.add("net.minecraft.launchwrapper.Launch");

        // User Properties
        if (pack.getGameVersion().equalsIgnoreCase("1.7.10")) {
            args.add("--userProperties");
            args.add("{}");
        }
        // Username
        args.add("--username");
        args.add(session.getAlias());

        // Version
        args.add("--version");
        args.add(pack.getForgeVersion());

        // Game Dir
        args.add("--gameDir");
        args.add(instanceDirectory.getPath());

        // Assets Dir
        args.add("--assetsDir");
        args.add(settings.getAssetsDirectory().toString());

        // Assets Index
        File assetsVersionJsonFile = Paths.get(settings.getVersionsDirectory().getPath(), pack.getGameVersion(), pack.getGameVersion() + ".json").toFile();
        String assetsVersion = FileUtils.readJsonFromFile(assetsVersionJsonFile).get("assets").getAsString();
        args.add("--assetIndex");
        args.add(assetsVersion);
        // UUID
        args.add("--uuid");
        args.add(session.getId().toString().replace("-", ""));
        // Access Token
        args.add("--accessToken");
        args.add(session.getAccessToken());

        // User Type
        args.add("--userType");
        args.add("mojang");
        // Tweak Class
        args.add("--tweakClass");
        args.add(!pack.getGameVersion().equals("1.7.10") ?
                "net.minecraftforge.fml.common.launcher.FMLTweaker" :
                "cpw.mods.fml.common.launcher.FMLTweaker");

        // Version Type
        args.add("--versionType");
        args.add("Forge");

        Thread gameThread = new Thread(() -> {
            try {
                if (Constants.VERBOSE){
                    System.out.println("---DIR---");
                    System.out.println(instanceDirectory);
                    System.out.println("---ARG---");
                    System.out.println(args);
                    System.out.println("---END---");
                }
                Process minecraft = new ProcessBuilder()
                        .directory(instanceDirectory)
                        //.inheritIO()
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectInput(ProcessBuilder.Redirect.INHERIT)
                        .command(args)
                        .start();
                Platform.runLater(() -> {
                    //Close install stage if it's open
                    Install.getStage().ifPresent(Stage::close);

                    //Minimize the main stage to the task bar
                    Main.getHome().getStage().close();
                    //Create system tray icon
                    SwingUtilities.invokeLater(() -> SystemTray.createIcon(pack));
                });
                StringBuilder buffer = new StringBuilder();
                try(InputStreamReader isr = new InputStreamReader(minecraft.getErrorStream());
                    BufferedReader br = new BufferedReader(isr)
                ){
                    String ln;
                    while((ln = br.readLine()) != null){
                        buffer.append(ln);
                    }
                }
                //Show main stage
                Platform.runLater(() -> Main.getHome().getStage().show());
                if (buffer.length()>0) Platform.runLater(()->new ErrorWindow(buffer.toString()).show());

                //Close system tray icon
                SwingUtilities.invokeLater(() -> SystemTray.getIcon().ifPresent(icon -> SystemTray.tray.remove(icon)));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                LaunchGame.isGameRunning = false;
            }
        });
        gameThread.start();
    }

    private static String getLibs(Modpack pack) {
        Config settings = Main.getConfig();
        StringBuilder libs = new StringBuilder();
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getForgeDirectory())).getAsJsonArray("forgeVersions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getForgeVersion()))
                libs.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        for (JsonElement jsonElement : FileUtils.readJsonFromFile(settings.getDirectoryManifest(settings.getVersionsDirectory())).getAsJsonArray("versions")) {
            if (jsonElement.getAsJsonObject().get("version").getAsString().equals(pack.getGameVersion()))
                libs.append(jsonElement.getAsJsonObject().get("classpathLibraries").getAsString().replace("\\\\", "\\") + ";");
        }
        libs.append(new File(settings.getVersionsDirectory().getPath() + File.separator + pack.getGameVersion() + File.separator + pack.getGameVersion() + ".jar").getPath());

        if (SystemUtils.IS_OS_UNIX) return libs.toString().replace(";", ":");
        return libs.toString();
    }
}
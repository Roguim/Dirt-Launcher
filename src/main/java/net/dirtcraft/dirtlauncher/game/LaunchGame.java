package net.dirtcraft.dirtlauncher.game;

import com.google.gson.JsonElement;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.cydhra.nidhogg.data.Session;
import net.dirtcraft.dirtlauncher.gui.dialog.ErrorWindow;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.gui.components.SystemTray;
import net.dirtcraft.dirtlauncher.Data.Config;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.game.objects.Listing;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.game.objects.ServerList;
import net.dirtcraft.dirtlauncher.utils.FileUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchGame {

    private static Logger logger = LogManager.getLogger("Launch Game");
    public static boolean isGameRunning = false;

    public static void loadServerList(Pack pack) {
        ServerList serverList = ServerList.builder(pack.getName());
        if (pack.isPixelmon()) {
            pack.getListings().ifPresent(listings -> {
                for (Listing listing : listings) {
                    serverList.addServer(listing.getIp(), listing.getName());
                }
            });
        } else
            serverList.addServer((pack.getCode() + ".DIRTCRAFT.GG").toUpperCase(), "§c§lDirtCraft §8- §d" + pack.getName());
        serverList.build();
    }

    public static void launchPack(Pack pack, Session session) {
        Config settings = Main.getConfig();
        final File instanceDirectory = new File(settings.getInstancesDirectory().getPath() + File.separator + pack.getFormattedName());

        List<String> args = new ArrayList<>();
        if (false && SystemUtils.IS_OS_UNIX) {
            args.add("/bin/sh");
            args.add("-c");
        }
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

        // Auto Join
        /*command.append("--server ");
        if (pack.isPixelmon()) command.append(pack.getCode() + ".dirtcraft.gg");
        else command.append("pixelmon.gg");
        command.append(" ");*/

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
                    logger.info("---DIR---");
                    logger.info(instanceDirectory);
                    logger.info("---ARG---");
                    logger.info(args);
                    logger.info("---END---");
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
                    Main.getInstance().getStage().close();
                    //Create system tray icon
                    SwingUtilities.invokeLater(() -> SystemTray.createIcon(pack));
                });
                try(InputStreamReader isr = new InputStreamReader(minecraft.getErrorStream());
                    BufferedReader br = new BufferedReader(isr)
                ){
                    String ln;
                    StringBuilder buffer = new StringBuilder();
                    while((ln = br.readLine()) != null){
                        buffer.append(ln);
                    }
                    if (buffer.length()>0) new ErrorWindow(buffer.toString()).show();
                }
                //Show main stage
                Platform.runLater(() -> Main.getInstance().getStage().show());

                //Close system tray icon
                SwingUtilities.invokeLater(() -> SystemTray.getIcon().ifPresent(icon -> SystemTray.tray.remove(icon)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        gameThread.start();
    }

    private static String getLibs(Pack pack) {
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
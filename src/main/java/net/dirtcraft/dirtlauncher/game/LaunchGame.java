package net.dirtcraft.dirtlauncher.game;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.configuration.manifests.AssetManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.ForgeManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.InstanceManifest;
import net.dirtcraft.dirtlauncher.configuration.manifests.VersionManifest;
import net.dirtcraft.dirtlauncher.data.DirtLauncher.Settings;
import net.dirtcraft.dirtlauncher.data.Minecraft.JavaVersion;
import net.dirtcraft.dirtlauncher.exceptions.InstanceException;
import net.dirtcraft.dirtlauncher.exceptions.LaunchException;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.game.serverlist.Listing;
import net.dirtcraft.dirtlauncher.game.serverlist.ServerList;
import net.dirtcraft.dirtlauncher.gui.components.SystemTray;
import net.dirtcraft.dirtlauncher.gui.dialog.ErrorWindow;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.JsonUtils;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LaunchGame {

    public static boolean isGameRunning = false;

    public static void loadServerList(Modpack pack) throws InstanceException {
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

    public static void launchPack(Modpack pack, Account session) throws LaunchException {
        ConfigurationManager configManager = Main.getConfig();
        Settings settings = configManager.getSettings();
        InstanceManifest.Entry instanceManifest = configManager.getInstanceManifest().get(pack).orElseThrow(()->new LaunchException("Instance manifest entry not present."));
        VersionManifest.Entry versionManifest = configManager.getVersionManifest().get(pack.getGameVersion()).orElseThrow(()->new LaunchException("Version manifest entry not present."));
        ForgeManifest.Entry forgeManifest = configManager.getForgeManifest().get(pack.getForgeVersion()).orElseThrow(()->new LaunchException("Forge manifest entry not present."));
        AssetManifest.Entry assetManifest = configManager.getAssetManifest().get(pack.getGameVersion()).orElseThrow(()->new LaunchException("Asset manifest entry not present."));
        JavaVersion version = versionManifest.getJavaVersion();
        if (version == null) version = JavaVersion.legacy; //for old installs
        File javaDir = new File(configManager.getJavaDirectory(), version.getFolder());
        final File instanceDirectory = instanceManifest.getDirectory().toFile();

        List<String> args = new ArrayList<>();
        String javaExecutable = SystemUtils.IS_OS_WINDOWS && !Constants.VERBOSE ? "javaw" : "java";
        String jvm = javaDir.toPath().resolve("bin").resolve(javaExecutable).toString();
        args.add(jvm);

        //args.add(configManager.getDefaultRuntime());

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
        String nativesPath = versionManifest.getNativesFolder().toString();
        args.add("-Djava.library.path=" + nativesPath);
        args.add("-Dorg.lwjgl.librarypath=" + nativesPath);
        args.add("-Dnet.java.games.input.librarypath=" + nativesPath);
        args.add("-Duser.home=" + instanceDirectory.getPath());

        // Classpath
        args.add("-cp");
        args.add(getLibs(versionManifest, forgeManifest));

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
        args.add(assetManifest.getAssetDirectory().toString());

        // Assets Index
        File assetsVersionJsonFile = versionManifest.getVersionManifestFile();
        String assetsVersion = JsonUtils.readJsonFromFile(assetsVersionJsonFile).get("assets").getAsString();
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
                Logger.INSTANCE.verbose("---DIR---");
                Logger.INSTANCE.verbose(instanceDirectory);
                Logger.INSTANCE.verbose("---ARG---");
                Logger.INSTANCE.verbose(args);
                Logger.INSTANCE.verbose("---END---");

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
                try (InputStreamReader isr = new InputStreamReader(minecraft.getErrorStream());
                     BufferedReader br = new BufferedReader(isr)
                ) {
                    String ln;
                    while ((ln = br.readLine()) != null) {
                        buffer.append(ln);
                    }
                }
                //Show main stage
                Platform.runLater(() -> Main.getHome().getStage().show());
                if (buffer.length() > 0) Platform.runLater(() -> new ErrorWindow(buffer.toString()).show());

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

    private static String getLibs(VersionManifest.Entry versionManifest, ForgeManifest.Entry forgeManifest) {
        char sep = SystemUtils.IS_OS_UNIX? ':' : ';';
        return String.valueOf(forgeManifest.getForgeJarFile()) + sep +
                forgeManifest.getLibs() + sep +
                versionManifest.getLibs() + sep +
                versionManifest.getVersionJarFile();
    }
}
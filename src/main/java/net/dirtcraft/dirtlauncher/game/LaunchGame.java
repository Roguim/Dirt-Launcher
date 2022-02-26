package net.dirtcraft.dirtlauncher.game;

import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.DirtLauncher;
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
import net.dirtcraft.dirtlauncher.utils.Launcher;
import net.dirtcraft.dirtlauncher.utils.LegacyLauncher;
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
        if (session == null) return;
        ConfigurationManager configManager = DirtLauncher.getConfig();
        Settings settings = configManager.getSettings();
        VersionManifest.Entry versionManifest = configManager.getVersionManifest().get(pack.getGameVersion()).orElseThrow(()->new LaunchException("Version manifest entry not present."));
        ForgeManifest.Entry forgeManifest = configManager.getForgeManifest().get(pack.getForgeVersion()).orElseThrow(()->new LaunchException("Forge manifest entry not present."));
        JavaVersion version = versionManifest.getJavaVersion() == null? JavaVersion.LEGACY : versionManifest.getJavaVersion();
        Thread gameThread = new Thread(() -> {
            try {
                Process minecraft;
                if (pack.getGameVersion().equals("1.16.5")) minecraft = new Launcher(pack, version)
                            .applyClasspath(forgeManifest.getLibs().split(";"))
                            .applyClasspath(versionManifest.getLibs().split(";"))
                            .launch(settings, session);
                else minecraft = new LegacyLauncher(pack, version)
                        .launchPack(settings, session);
                Platform.runLater(() -> {
                    //Close install stage if it's open
                    Install.getStage().ifPresent(Stage::close);

                    //Minimize the main stage to the task bar
                    DirtLauncher.getHome().getStage().close();
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
                Platform.runLater(() -> DirtLauncher.getHome().getStage().show());
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
}
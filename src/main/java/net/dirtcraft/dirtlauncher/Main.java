package net.dirtcraft.dirtlauncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.game.authentification.AccountManager;
import net.dirtcraft.dirtlauncher.gui.dialog.Update;
import net.dirtcraft.dirtlauncher.gui.home.Home;
import net.dirtcraft.dirtlauncher.gui.home.toolbar.Settings;
import net.dirtcraft.dirtlauncher.utils.UpdateHelper;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    public static final Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
    private static List<String> options;
    private static long x = System.currentTimeMillis();
    private static CompletableFuture<Void> stageInit = null;
    private static Config config = null;
    private static Settings settingsMenu = null;
    private static AccountManager accounts = null;
    private static Home home = null;
    private static Path launcherDirectory;
    public static Gson gson;

    public static void main(String[] args) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        options = Arrays.asList(args);
        initLauncherDirectory();
        stageInit = CompletableFuture.runAsync(Main::preInitHome);
        CompletableFuture.runAsync(Main::initAccountManager);
        CompletableFuture.runAsync(Main::initSettingsAndUpdater);
        CompletableFuture.runAsync(Main::postUpdateCleanup);
        launch(args);
    }

    public static AccountManager getAccounts() {
        return accounts;
    }

    public static Settings getSettingsMenu() {
        return settingsMenu;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);
        stageInit.get();
        home.getStage().show();
        home.update();
        System.out.println("Launching @ " + (System.currentTimeMillis() - x) + "ms");
    }

    public static Home getHome() {
        if (home == null) {
            home = new Home();
        }
        return home;
    }

    public static Config getConfig() {
        return config;
    }

    public static List<String> getOptions(){
        return options;
    }

    public static Path getLauncherDirectory(){
        return launcherDirectory;
    }

    public static void preInitHome(){
        try {
            home = new Home();
            System.out.println("Scene pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void initAccountManager() {
        accounts = new AccountManager(launcherDirectory);
        System.out.println("Account manager initialized @ " + (System.currentTimeMillis() - x) + "ms");
    }

    public static void initSettingsAndUpdater(){
        config = new Config(launcherDirectory, options);
        System.out.println("Config initialized @ " + (System.currentTimeMillis() - x) + "ms");
        settingsMenu = new Settings(config);
        System.out.println("Settings menu pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
        try {
            if (options.contains("-update") && Update.hasUpdate()) new UpdateHelper();
            if (Update.hasUpdate()) Platform.runLater(Update::showStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postUpdateCleanup(){
        try {
            File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            String bootstrapName = "UpdateBootstrapper.class";
            final File bootstrap = new File(currentJar, bootstrapName);
            final boolean updated = bootstrap.delete();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void initLauncherDirectory(){
        if (options.contains("-installed") || options.contains("-portable"))
            try {
                launcherDirectory = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            } catch (Exception e){ throw new Error(e); }
        else if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            launcherDirectory = Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            launcherDirectory = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else launcherDirectory = Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
        System.out.println("Block Start @ " + (System.currentTimeMillis() - x) + "ms");
    }

    private void run(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }
}

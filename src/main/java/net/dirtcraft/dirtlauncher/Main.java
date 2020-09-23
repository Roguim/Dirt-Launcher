package net.dirtcraft.dirtlauncher;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.configuration.Config;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.data.serializers.MultiMapAdapter;
import net.dirtcraft.dirtlauncher.game.authentification.AccountManager;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.dialog.Update;
import net.dirtcraft.dirtlauncher.gui.home.Home;
import net.dirtcraft.dirtlauncher.gui.home.toolbar.Settings;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.providers.CurseProvider;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private static long x = System.currentTimeMillis();
    public static final Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
    private static Path launcherDirectory;
    private static List<String> options;
    private static CompletableFuture<Home> home = null;
    private static CompletableFuture<Config> config = null;
    private static CompletableFuture<Settings> settingsMenu = null;
    private static CompletableFuture<AccountManager> accounts = null;
    public static Gson gson;

    public static void main(String[] args) {
        //noinspection rawtypes
        gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Multimap.class, new MultiMapAdapter())
                .create();
        options = Arrays.asList(args);
        initLauncherDirectory();
        home = CompletableFuture.supplyAsync(Main::preInitHome);
        accounts = CompletableFuture.supplyAsync(Main::initAccountManager);
        config = CompletableFuture.supplyAsync(Main::initConfig);
        settingsMenu = config.thenApply(Main::initSettings);
        settingsMenu.thenRun(Main::checkUpdate);
        CompletableFuture.runAsync(Main::postUpdateCleanup);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        Home home = Main.home.join();
        home.getStage().show();
        home.update();
        Logger.INSTANCE.info("Launching @ " + (System.currentTimeMillis() - x) + "ms");
        //testMethodPleaseIgnore();
    }

    public static AccountManager getAccounts() {
        try {
            return accounts.get();
        } catch (Throwable e){
            throw new Error(e);
        }
    }

    public static Settings getSettingsMenu() {
        try {
            return settingsMenu.get();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Home getHome() {
        try {
            return home.get();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static Config getConfig() {
        try {
            return config.get();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static List<String> getOptions(){
        return options;
    }

    public static Path getLauncherDirectory(){
        return launcherDirectory;
    }

    private static Home preInitHome(){
        try {
            Home home = new Home();
            Logger.INSTANCE.debug("Scene pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
            return home;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static AccountManager initAccountManager() {
        AccountManager accounts = new AccountManager(launcherDirectory);
        Logger.INSTANCE.debug("Account manager initialized @ " + (System.currentTimeMillis() - x) + "ms");
        return accounts;
    }

    private static Config initConfig() {
        Config config = new Config(launcherDirectory, options);
        Logger.INSTANCE.debug("Config initialized @ " + (System.currentTimeMillis() - x) + "ms");
        return config;
    }
    private static Settings initSettings(Config config) {
        Settings settingsMenu = new Settings(config);
        Logger.INSTANCE.debug("Settings menu pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
        return settingsMenu;
    }

    private static void checkUpdate(){
        try {
            if (options.contains("-update") && Update.hasUpdate()) MiscUtils.updateLauncher();
            if (Update.hasUpdate()) Platform.runLater(Update::showStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void postUpdateCleanup(){
        try {
            File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            String bootstrapName = Constants.BOOTSTRAP_JAR;
            final File bootstrap = new File(currentJar, bootstrapName);
            final boolean updated = bootstrap.delete();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void initLauncherDirectory(){
        if (options.contains("-installed") || options.contains("-portable"))
            try {
                launcherDirectory = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            } catch (Exception e){ throw new Error(e); }
        else if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            launcherDirectory = Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            launcherDirectory = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else launcherDirectory = Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
        Logger.INSTANCE.debug("Block Start @ " + (System.currentTimeMillis() - x) + "ms");
    }

    private static void logInfo(){
        final Logger logger = Logger.getInstance();
        logger.debug("Launcher Version: " + Constants.LAUNCHER_VERSION);
        logger.debug("Bootstrap Version: " + Constants.BOOTSTRAP_JAR);
        logger.debug("Update Source: " + Constants.UPDATE_URL);
        logger.debug("Modpack Service: " + Constants.PACK_JSON_URL);
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

    private void testMethodPleaseIgnore() {
        CurseProvider.InstanceManager.getInstance(CurseProvider.class).ifPresent(curseProvider -> {
            try {
                CompletableFuture<Optional<Modpack>> a = curseProvider.getModpackFromUrlAsync(new URL("https://www.curseforge.com/minecraft/modpacks/infinityevolved-reloaded"));
                a.whenComplete((z,b)->System.out.println("!!!"));
            } catch (MalformedURLException e){
                e.printStackTrace();
            }
        });
    }
}

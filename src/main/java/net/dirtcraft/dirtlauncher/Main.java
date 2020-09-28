package net.dirtcraft.dirtlauncher;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.configuration.ConfigurationManager;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.data.serializers.MultiMapAdapter;
import net.dirtcraft.dirtlauncher.game.authentification.AccountManager;
import net.dirtcraft.dirtlauncher.gui.dialog.Update;
import net.dirtcraft.dirtlauncher.gui.home.Home;
import net.dirtcraft.dirtlauncher.gui.home.toolbar.Settings;
import net.dirtcraft.dirtlauncher.logging.Logger;
import net.dirtcraft.dirtlauncher.utils.WebUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private static long x = System.currentTimeMillis();
    public static final Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
    private static CompletableFuture<ConfigurationManager> config = null;
    private static CompletableFuture<AccountManager> accounts = null;
    private static CompletableFuture<Settings> settingsMenu = null;
    private static CompletableFuture<Home> home = null;
    private static Path launcherDirectory;
    private static List<String> options;
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
        launcherDirectory = getLauncherDirectory(options);
        home = CompletableFuture
                .supplyAsync(Home::new)
                .whenComplete(Main::announceCompletion);
        accounts = CompletableFuture
                .supplyAsync(()->new AccountManager(launcherDirectory))
                .whenComplete(Main::announceCompletion);
        config = CompletableFuture
                .supplyAsync(()->new ConfigurationManager(launcherDirectory, options))
                .whenComplete(Main::announceCompletion);
        settingsMenu = config
                .thenApply(Settings::new)
                .whenComplete(Main::announceCompletion);
        settingsMenu.thenRun(Update::checkForUpdates);
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
    }

    private static <T> void announceCompletion(T t, Throwable e){
        if (!Constants.VERBOSE) return;
        final long ms = System.currentTimeMillis() - x;
        final String clazz = t.getClass().getSimpleName();
        System.out.println(String.format("%s initialized @ %sms", clazz, ms));
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

    private static Path getLauncherDirectory(List<String> options){
        if (options.contains("-installed") || options.contains("-portable"))
            try {
                return Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            } catch (Exception e){ throw new Error(e); }
        else if (SystemUtils.IS_OS_WINDOWS)// If the host OS is windows, use AppData
            return Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        else if (SystemUtils.IS_OS_MAC)// If the host OS is mac, use the user's Application Support directory
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
        else return Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");
        //Logger.INSTANCE.debug("Block Start @ " + (System.currentTimeMillis() - x) + "ms");
    }

    public static AccountManager getAccounts() {
        return accounts.join();
    }

    public static Settings getSettingsMenu() {
        return settingsMenu.join();
    }

    public static Home getHome() {
        return home.join();
    }

    public static ConfigurationManager getConfig() {
        return config.join();
    }

    public static List<String> getOptions(){
        return options;
    }

    public static Path getLauncherDirectory(){
        return launcherDirectory;
    }
}

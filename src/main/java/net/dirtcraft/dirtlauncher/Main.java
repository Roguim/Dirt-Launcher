package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.stages.Home;
import net.dirtcraft.dirtlauncher.stages.Settings;
import net.dirtcraft.dirtlauncher.stages.Update;
import net.dirtcraft.dirtlauncher.backend.Data.Accounts;
import net.dirtcraft.dirtlauncher.backend.Data.Config;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {

    private static volatile Config config = null;
    private static Logger logger = null;
    private static Main instance;
    private static List<String> options;
    private static boolean updated = false;
    private static volatile Accounts accounts = null;
    private static volatile Home stage = null;
    private static volatile Home.Builder homeBuilder = null;
    private static long x;
    private static CompletableFuture stageInit = null;

    public static void main(String[] args) {
        x = System.currentTimeMillis();
        options = Arrays.asList(args);
        final Path launcherDirectory;
        // If we are using a portable install, we use the current folder.
        if (options.contains("-portable"))
            launcherDirectory = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            // If the host OS is windows, use AppData
        else if (SystemUtils.IS_OS_WINDOWS)
            launcherDirectory = Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
            // If the host OS is linux, use the user's Application Support directory
        else if (SystemUtils.IS_OS_MAC)
            launcherDirectory = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "DirtCraft", "DirtLauncher");
            // Otherwise, we can assume the host OS is probably linux, so we'll use their application folder
        else launcherDirectory = Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");

        //init landing stage async
        stageInit = CompletableFuture.runAsync(() -> {
            try {
                homeBuilder = new Home.Builder();
                System.out.println("Scene pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        });

        //pre-init accounts async
        CompletableFuture.runAsync(() -> {
            accounts = new Accounts(launcherDirectory);
                while (!stageInit.isDone() || !accounts.isReady()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) { }
                }
            System.out.println("Account manager initialized @ " + (System.currentTimeMillis() - x) + "ms");
            Platform.runLater(()->stage.getLoginBar().setInputs());
        });

        //init config async
        CompletableFuture.runAsync(() -> {
            config = new Config(launcherDirectory);
        });

        //init logger async
        CompletableFuture.runAsync(() -> {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
            final Date date = new Date();
            final String fname = dateFormat.format(date);
            System.setProperty("log4j.saveDirectory", launcherDirectory.resolve("logs").resolve(fname + ".log").toString());
            logger = LogManager.getLogger(Main.class);
            System.out.println("Logger initialized @ " + (System.currentTimeMillis() - x) + "ms");
            //Grab a list of log files and delete all but the last 5.
            final List<File> logFiles = Arrays.asList(Objects.requireNonNull(config.getLogDirectory().toFile().listFiles()));
            logFiles.sort(Collections.reverseOrder());
            for (int i = 0; i < logFiles.size(); i++) {
                if (i >= 5) {
                    if (!logFiles.get(i).delete())
                        logger.warn("failed to delete old log file: " + logFiles.get(i).getName());
                }
            }
            try {
                File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                String bootstrapName = "UpdateBootstrapper.class";
                final File bootstrap = new File(currentJar, bootstrapName);
                if (bootstrap.delete()) {
                    updated = true;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });

        //pre-init config async
        //TODO make faster - takes 3s, scene takes 1s. this is a slowdown
        CompletableFuture.runAsync(() -> {
            Settings.loadSettings();
            System.out.println("Settings menu pre-rendered @ " + (System.currentTimeMillis() - x) + "ms");
            try {
                if (Update.hasUpdate()) Platform.runLater(Update::showStage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Launch the application
        launch(args);
    }

    public static Accounts getAccounts() {
        return accounts;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        Platform.setImplicitExit(false);

        while (!stageInit.isDone()){
            Thread.sleep(50);
        }
        stage = homeBuilder.build();
        stage.show();
        System.out.println("Launching @ " + (System.currentTimeMillis() - x) + "ms");
    }

    @Override
    public void stop() {
        LogManager.shutdown();
    }

    public Stage getStage() {
        return stage;
    }

    public static Main getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }

    public static List<String> getOptions(){
        return options;
    }
}

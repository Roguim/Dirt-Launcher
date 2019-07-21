package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Controllers.Update;
import net.dirtcraft.dirtlauncher.backend.config.Config;
import net.dirtcraft.dirtlauncher.backend.config.Constants;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Main extends Application {

    private static Config settings = null;
    private static Logger logger = null;
    private static Future<Parent> root;
    private static Main instance;
    private Stage stage;

    public static void main(String[] args) {
        final Path launcherDirectory;
        // If it's windows, use AppData
        if (SystemUtils.IS_OS_WINDOWS) launcherDirectory = Paths.get(System.getenv("AppData"), "DirtCraft", "DirtLauncher");
        // If it's linux, use the user's Application Support directory
        else if (SystemUtils.IS_OS_MAC) launcherDirectory =  Paths.get(System.getProperty("user.home") , "Library" , "Application Support", "DirtCraft", "DirtLauncher");
        // Otherwise, we can assume it's probably linux, so we'll use their application folder
        else launcherDirectory =  Paths.get(System.getProperty("user.home"), "DirtCraft", "DirtLauncher");

        //init settings async
        new Thread(()->settings = new Config(launcherDirectory)).start();

        //init logger async
        new Thread(()->{
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
            final Date date = new Date();
            final String fname = dateFormat.format(date);
            System.setProperty("log4j.saveDirectory", launcherDirectory.resolve("logs").resolve(fname+".log").toString());
            logger = LogManager.getLogger(Main.class);
            logger.info("Logger logging, App starting.");
            //Grab a list of log files and delete all but the last 5.
            final List<File> logFiles = Arrays.asList(Objects.requireNonNull(settings.getLogDirectory().toFile().listFiles()));
            logFiles.sort(Collections.reverseOrder());
            for(int i = 0; i < logFiles.size(); i++){
                if (i>=5){
                    if (!logFiles.get(i).delete()) logger.warn("failed to delete old log file: " + logFiles.get(i).getName());
                }
            }
        }).start();

        //pre-init settings async
        new Thread(()->{
            net.dirtcraft.dirtlauncher.Controllers.Settings.loadSettings();
            try {
                if (Update.hasUpdate()) Platform.runLater(Update::showStage);
            } catch (IOException e){
                e.printStackTrace();
            }
        }).start();

        //pre-init main stage async.
        root = CompletableFuture.supplyAsync(()->{
            try {
                return FXMLLoader.load(MiscUtils.getResourceURL(Constants.SCENES, "main.fxml"));
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });

        // Launch the application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        Platform.setImplicitExit(false);
        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(MiscUtils.getImage(Constants.ICONS, "main.png"));
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest(event -> {
            if (!LaunchGame.isGameRunning) Platform.exit();
        });
        Scene scene = new Scene(root.get(), MiscUtils.screenDimension.getWidth() / 1.15, MiscUtils.screenDimension.getHeight() / 1.35);
        primaryStage.setScene(scene);
        primaryStage.show();
        stage = primaryStage;
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

    public static Config getSettings() {
        return settings;
    }
}

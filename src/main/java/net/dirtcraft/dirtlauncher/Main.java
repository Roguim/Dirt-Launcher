package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Controllers.Settings;
import net.dirtcraft.dirtlauncher.Controllers.Update;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    private static Logger logger;
    private static Main instance;
    private Stage stage;

    public static void main(String[] args) {
        for (String s : args) switch (s){
            case "-portable":Internal.PORTABLE = true; break;
            case "-verbose":Internal.VERBOSE = true; break;
        }
        System.setProperty("log4j.saveDirectory", Directories.getLog().toString());
        logger = LogManager.getLogger(Main.class);
        logger.info("Logger logging, App starting.");
        new Thread(()->{
            //Grab a list of log files and delete all but the last 5.
            List<File> logFiles = Arrays.asList(Objects.requireNonNull(Directories.getLogDirectory().toFile().listFiles()));
            logFiles.sort(Collections.reverseOrder());
            for(int i = 0; i < logFiles.size(); i++){
                if (i>=5){
                    boolean success = logFiles.get(i).delete();
                    if (!success) logger.warn("failed to delete old log file: " + logFiles.get(i).getName());
                }
            }
            // Ensure that the application folders are created
            Directories.getLauncherDirectory().mkdirs();
            FileUtils.initGameDirectory();
        }).start();
        // Launch the application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Platform.setImplicitExit(false);

        Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "main.fxml"));
        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "main.png"));

        Scene scene = new Scene(root, MiscUtils.screenDimension.getWidth() / 1.15, MiscUtils.screenDimension.getHeight() / 1.35);

        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            if (!LaunchGame.isGameRunning) Platform.exit();
        });
        stage = primaryStage;

        stage.show();

        new Thread(()->{
            Settings.loadSettings();
            try {
                if (Update.hasUpdate()) Platform.runLater(Update::showStage);
            } catch (IOException e){
                logger.error(e);
            }
        }).start();

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
}

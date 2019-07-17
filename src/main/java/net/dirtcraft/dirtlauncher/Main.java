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

public class Main extends Application {

    private static Logger logger;
    private static Main instance;
    private Stage stage;

    public static Logger getLogger() {
        return logger;
    }

    public static void main(String[] args) {
        System.setProperty("log4j.saveDirectory", Directories.getLog().toString());
        logger = LogManager.getLogger(Main.class);
        logger.info("Logger logging, App starting.");
        // Ensure that the application folders are created
        Directories.getLauncherDirectory().mkdirs();
        FileUtils.initGameDirectory();
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

        Settings.loadSettings();
        if (Update.hasUpdate()) Update.showStage();

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
}

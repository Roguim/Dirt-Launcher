package net.dirtcraft.dirtlauncher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Controllers.Settings;
import net.dirtcraft.dirtlauncher.Controllers.Update;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
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
        Directories.getInstallDirectory().mkdirs();
        Directories.getInstancesDirectory().mkdirs();
        Directories.getVersionsDirectory().mkdirs();
        Directories.getAssetsDirectory().mkdirs();
        Directories.getForgeDirectory().mkdirs();
        // Ensure that all required manifests are created
        if (!Directories.getConfiguration().exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
            emptyManifest.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
            emptyManifest.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
            FileUtils.writeJsonToFile(Directories.getConfiguration(), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getInstancesDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getVersionsDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getAssetsDirectory()), emptyManifest);
        }
        if(!Directories.getDirectoryManifest(Directories.getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeJsonToFile(Directories.getDirectoryManifest(Directories.getForgeDirectory()), emptyManifest);
        }
        // Launch the application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "main.fxml"));

        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "main.png"));

        Scene scene = new Scene(root, MiscUtils.screenDimension.getWidth() / 1.15, MiscUtils.screenDimension.getHeight() / 1.35);

        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.setScene(scene);
        stage = primaryStage;


        stage.show();

        Settings.loadSettings();
        if (Update.hasUpdate()) Update.showStage();

    }

    @Override
    public void stop(){
        LogManager.shutdown();
    }

    public Stage getStage() {
        return stage;
    }

    public static Main getInstance() {
        return instance;
    }
}

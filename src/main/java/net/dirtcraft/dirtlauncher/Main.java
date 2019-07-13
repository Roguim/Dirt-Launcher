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
import net.dirtcraft.dirtlauncher.backend.config.Directory;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static Logger logger;
    private static Main instance;
    private Stage stage;


    public static void main(String[] args) {
        System.setProperty("log4j.saveDirectory", Directory.getLog().toString());
        logger = LogManager.getLogger(Main.class);
        logger.info("Logger logging, App starting.");
        // Ensure that the application folders are created
        Directory.getInstallDirectory().mkdirs();
        Directory.getInstancesDirectory().mkdirs();
        Directory.getVersionsDirectory().mkdirs();
        Directory.getAssetsDirectory().mkdirs();
        Directory.getForgeDirectory().mkdirs();
        // Ensure that all required manifests are created
        if (!Directory.getConfiguration().exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
            emptyManifest.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
            emptyManifest.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
            FileUtils.writeJsonToFile(Directory.getConfiguration(), emptyManifest);
        }
        if(!Directory.getDirectoryManifest(Directory.getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeJsonToFile(Directory.getDirectoryManifest(Directory.getInstancesDirectory()), emptyManifest);
        }
        if(!Directory.getDirectoryManifest(Directory.getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeJsonToFile(Directory.getDirectoryManifest(Directory.getVersionsDirectory()), emptyManifest);
        }
        if(!Directory.getDirectoryManifest(Directory.getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeJsonToFile(Directory.getDirectoryManifest(Directory.getAssetsDirectory()), emptyManifest);
        }
        if(!Directory.getDirectoryManifest(Directory.getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeJsonToFile(Directory.getDirectoryManifest(Directory.getForgeDirectory()), emptyManifest);
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

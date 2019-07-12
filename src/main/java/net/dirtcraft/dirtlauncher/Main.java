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
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;

public class Main extends Application {

    private static Main instance;
    private Stage stage;


    public static void main(String[] args) {

        // Ensure that the application folders are created
        Paths.getInstallDirectory().mkdirs();
        Paths.getInstancesDirectory().mkdirs();
        Paths.getVersionsDirectory().mkdirs();
        Paths.getAssetsDirectory().mkdirs();
        Paths.getForgeDirectory().mkdirs();
        // Ensure that all required manifests are created
        if (!Paths.getConfiguration().exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
            emptyManifest.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
            emptyManifest.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
            FileUtils.writeJsonToFile(Paths.getConfiguration(), emptyManifest);
        }
        if(!Paths.getDirectoryManifest(Paths.getInstallDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.addProperty("version", "0.0.0");
            FileUtils.writeJsonToFile(Paths.getDirectoryManifest(Paths.getInstallDirectory()), emptyManifest);
        }
        if(!Paths.getDirectoryManifest(Paths.getInstancesDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("packs", new JsonArray());
            FileUtils.writeJsonToFile(Paths.getDirectoryManifest(Paths.getInstancesDirectory()), emptyManifest);
        }
        if(!Paths.getDirectoryManifest(Paths.getVersionsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("versions", new JsonArray());
            FileUtils.writeJsonToFile(Paths.getDirectoryManifest(Paths.getVersionsDirectory()), emptyManifest);
        }
        if(!Paths.getDirectoryManifest(Paths.getAssetsDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("assets", new JsonArray());
            FileUtils.writeJsonToFile(Paths.getDirectoryManifest(Paths.getAssetsDirectory()), emptyManifest);
        }
        if(!Paths.getDirectoryManifest(Paths.getForgeDirectory()).exists()) {
            JsonObject emptyManifest = new JsonObject();
            emptyManifest.add("forgeVersions", new JsonArray());
            FileUtils.writeJsonToFile(Paths.getDirectoryManifest(Paths.getForgeDirectory()), emptyManifest);
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

    public Stage getStage() {
        return stage;
    }

    public static Main getInstance() {
        return instance;
    }
}

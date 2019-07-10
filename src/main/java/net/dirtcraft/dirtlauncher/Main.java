package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.Paths;
import net.dirtcraft.dirtlauncher.backend.utils.Utility;

import java.awt.*;

public class Main extends Application {

    private static Main instance;
    private Stage stage;


    public static void main(String[] args) {
        // Ensure that the application folders are created
        Paths.getInstallDirectory().mkdirs();
        // Launch the application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Parent root = FXMLLoader.load(Utility.getResourceURL(Internal.SCENES, "main.fxml"));

        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(Utility.getImage(Internal.ICONS, "main.png"));

        Scene scene = new Scene(root, screenSize.getWidth() / 1.15, screenSize.getHeight() / 1.35);

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);

        primaryStage.show();

        stage = primaryStage;
    }

    public Stage getStage() {
        return stage;
    }

    public static Main getInstance() {
        return instance;
    }
}

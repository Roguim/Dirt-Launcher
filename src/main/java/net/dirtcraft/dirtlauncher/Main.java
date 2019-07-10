package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;

public class Main extends Application {

    private static Main instance;
    private Stage stage;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));

        root.getStylesheets().add("https://fonts.gstatic.com/s/oleoscript/v7/rax5HieDvtMOe0iICsUccChdu0_y8zac.woff2");
        root.getStylesheets().add("https://fonts.gstatic.com/s/bevan/v10/4iCj6KZ0a9NXjG8dWCvZtUSI.woff2");

        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(new Image(getClass().getResourceAsStream("/icon.png")));

        Scene scene = new Scene(root, screenSize.getWidth() / 1.15, screenSize.getHeight() / 1.35);
        //scene.getStylesheets().add("resources/sidebar.css");

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

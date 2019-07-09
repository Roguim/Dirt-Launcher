package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;

public class Main extends Application {

    private Stage stage;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Parent root = FXMLLoader.load(getClass().getResource("resources" + File.separator + "sample.fxml"));

        primaryStage.setTitle("Dirt Launcher");
        primaryStage.getIcons().setAll(new Image(getClass().getResourceAsStream("resources" + File.separator + "icon.png")));

        Scene scene = new Scene(root, screenSize.width, screenSize.height);
        //scene.getStylesheets().add("resources/sidebar.css");

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);



        primaryStage.show();

        stage = primaryStage;
    }

    public Stage getStage() {
        return stage;
    }
}

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

    private Stage stage;


    public static void main(String[] args) {
        launch(args);
        //new PackRegistry();
        //Verification.login(null, null);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Parent root = FXMLLoader.load(getClass().getResource("resources/sample.fxml"));

        primaryStage.setTitle("Dirt Launcher");

        Scene scene = new Scene(root, screenSize.width, screenSize.height);
        //scene.getStylesheets().add("resources/sidebar.css");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);



        primaryStage.show();

        stage = primaryStage;
    }

    public Stage getStage() {
        return stage;
    }
}

package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class Main extends Application {


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
        //scene.getStylesheets().add("sidebar.css");


        primaryStage.setScene(scene);
        primaryStage.show();
    }

}

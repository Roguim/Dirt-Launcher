package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        /*Controller controller = new Controller();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        loader.setController(controller);
        Parent root = loader.load();*/

        //FXMLLoader loader = new FXMLLoader(Main.class.getResource("../../../../resources/sample.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("resources/sample.fxml"));

        primaryStage.setFullScreen(true);

        primaryStage.setTitle("Dirt Launcher");

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}

package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    String s = "sample.fxml";

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
        System.out.println("Test = " + getClass().getResource(s));
        Parent root = FXMLLoader.load(getClass().getResource(s));

        primaryStage.setTitle("Dirt Launcher");

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

}

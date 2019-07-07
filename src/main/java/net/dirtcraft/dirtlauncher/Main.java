package net.dirtcraft.dirtlauncher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Events.ButtonClick;

public class Main extends Application {

    public Button button;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Dirt Launcher");

        button = new Button();
        button.setText("This is a button");

        button.setOnAction(new ButtonClick(this));

        StackPane layout = new StackPane();
        layout.getChildren().add(button);

        Scene scene = new Scene(layout, 300, 400);

        primaryStage.setScene(scene);
        primaryStage.show();


        /*
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/
    }

}

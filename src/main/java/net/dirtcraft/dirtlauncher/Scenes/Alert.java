package net.dirtcraft.dirtlauncher.Scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;

public class Alert {

    private final Main main;

    public Alert(Main main) {
        this.main = main;
    }

    public void getAlert(String title, String message, String button) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinHeight(500);
        window.setMinWidth(800);

        Label label = new Label(message);

        Button closeButton = new Button(button);
        closeButton.setOnAction(event -> {
            window.close();
        });

        VBox layout = new VBox(50);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);

        window.setScene(new Scene(layout, 100, 300));
        window.show();

    }

}

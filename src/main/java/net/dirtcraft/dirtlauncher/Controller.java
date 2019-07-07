package net.dirtcraft.dirtlauncher;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class Controller {

    @FXML
    void buttonClicked(MouseEvent event) {
        System.out.println("MOUSE EVENT CLICKED");
    }

    @FXML
    void test(ActionEvent event) {
        System.out.println("TESTING123");
    }

}
package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class Controller {

    @FXML
    private ListView listView;

    boolean hasClicked = false;

    @FXML
    void buttonClicked(MouseEvent event) {

        ObservableList<String> list = FXCollections.<String>observableArrayList("FTB Interactions", "FTB Revelation", "Pixelmon Reforged", "DirtCraft's DirtBOT");
        if (!hasClicked) {
            listView.getItems().addAll(list);
            hasClicked = true;
        }

        System.out.println("MOUSE EVENT CLICKED");
    }

    @FXML
    void test(ActionEvent event) {
        System.out.println("TESTING123");
    }

}
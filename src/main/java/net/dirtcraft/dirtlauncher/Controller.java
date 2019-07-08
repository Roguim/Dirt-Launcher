package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class Controller {

    @FXML
    private ListView listView;

    @FXML
    private HBox topPane;

    boolean hasClicked = false;

    @FXML
    void buttonClicked(MouseEvent event) throws IOException {

        if (!hasClicked) {
            //Main.setListView(listView);

            final ObservableList<String> list = FXCollections.<String>observableArrayList(
                    "FTB Interactions\nCreated By: FTB Team\nPlays: N/A\nDownloads: N/A",
                    "FTB Revelation\nCreated By: tfox83\nPlays: N/A\nDownloads: N/A",
                    "Pixelmon Reforged\nPlays: N/A\nDownloads: N/A",
                    "DirtCraft's DirtBOT\nCreated By: TechDweebGaming & juliann");

            listView.applyCss();

            listView.getItems().addAll(list);
            //listView.getItems().addAll(list);
            hasClicked = true;
        }

        System.out.println("MOUSE EVENT CLICKED");
    }

    @FXML
    void test(ActionEvent event) {
        System.out.println("TESTING123");
    }

}
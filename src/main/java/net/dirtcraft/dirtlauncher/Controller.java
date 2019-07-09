package net.dirtcraft.dirtlauncher;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import net.dirtcraft.dirtlauncher.backend.Utils.Fetch;

public class Controller {

    private Fetch fetch;


    @FXML
    private ListView listView;

    @FXML
    private HBox topPane;

    /*
    private final ObservableList<String> list = FXCollections.<String>observableArrayList(
            "FTB Interactions\nCreated By: FTB Team\nPlays: N/A\nDownloads: N/A",
            "FTB Revelation\nCreated By: tfox83\nPlays: N/A\nDownloads: N/A",
            "Pixelmon Reforged\nPlays: N/A\nDownloads: N/A",
            "DirtCraft's DirtBOT\nCreated By: TechDweebGaming & juliann");*/

    boolean hasClicked = false;

    @FXML
    private void initialize() {
        fetch = new Fetch();

        listView.setItems(fetch.getPacks());
        listView.setPrefHeight(500D);
    }

    @FXML
    private void buttonClicked(MouseEvent event) {

        if (!hasClicked) {
            //Main.setListView(listView)

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
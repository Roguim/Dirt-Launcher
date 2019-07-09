package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.Utils.Fetch;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Controller {

    private Fetch fetch;


    @FXML
    private ListView<Pack> listView;

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

        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        listView.setCellFactory(useless->new PackCellFactory());
        listView.setItems(packs);
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
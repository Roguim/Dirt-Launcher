package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Controller {

    @FXML
    private ListView<Pack> listView;

    @FXML
    private HBox topPane;

    @FXML
    private WebView webView;

    @FXML
    private Text headerText;

    @FXML
    private void initialize() {


        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        listView.getStyleClass().add("packlist");
        listView.setCellFactory(useless -> new PackCellFactory());
        listView.setItems(packs);

        webView.getEngine().load("https://dirtcraft.net/");

    }

}
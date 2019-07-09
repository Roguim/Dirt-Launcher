package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.Utils.Fetch;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Controller {


    public static final String EVENT_TYPE_CLICK = "click";

    private Fetch fetch;


    @FXML
    private ListView<Pack> listView;

    @FXML
    private HBox topPane;

    @FXML
    private WebView webView;

    @FXML
    private void initialize() {
        fetch = new Fetch();

        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        listView.getStyleClass().add("packlist");
        listView.setCellFactory(useless -> new PackCellFactory());
        listView.setItems(packs);

        //webView.getEngine().load("https://www.dirtcraft.net/");

        //Hyperlink hyperlink = new Hyperlink("Store");
        WebEngine engine = webView.getEngine();

        engine.load("https://dirtcraft.net/");

    }

}
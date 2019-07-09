package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.data.PackCellFactory;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Controller {

    @FXML
    private ListView<Pack> packList;

    @FXML
    private HBox topPane;

    @FXML
    private WebView webView;

    @FXML
    private Text headerText;

    @FXML
    private Pane launchBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button playButton;

    @FXML
    private void initialize() {


        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        packs.addAll(PackRegistry.getPacks());
        packList.getStyleClass().add("packlist");
        packList.setCellFactory(useless -> new PackCellFactory());
        packList.setItems(packs);

        webView.getEngine().load("https://dirtcraft.net/");

    }

}
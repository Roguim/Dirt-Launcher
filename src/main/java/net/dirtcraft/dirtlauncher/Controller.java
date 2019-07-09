package net.dirtcraft.dirtlauncher;


import com.google.common.base.Strings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.JsonUtils.Pack;
import net.dirtcraft.dirtlauncher.backend.data.LoginButtonHandler;
import net.dirtcraft.dirtlauncher.backend.data.PackCellFactory;
import net.dirtcraft.dirtlauncher.backend.data.PackRegistry;

public class Controller {
    private static Controller instance;

    @FXML
    private ListView<Pack> packList;

    @FXML
    private WebView webView;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button playButton;

    @FXML
    private GridPane loginBox;

    public static Controller getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {

        instance = this;
        playButton.setDisable(true);
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        packs.addAll(PackRegistry.getPacks());
        packList.getStyleClass().add("packlist");
        packList.setCellFactory(useless -> new PackCellFactory());
        packList.setItems(packs);

        webView.getEngine().load("https://dirtcraft.net/");

    }

    @FXML
    private void onTabPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB) return;
        Object source = event.getSource();
        if (source == usernameField) passwordField.requestFocus();
        if (source == passwordField) usernameField.requestFocus();
        event.consume();
    }

    @FXML
    private void onEnterPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) return;
        if (playButton.isDisabled()) return;
        if (Strings.isNullOrEmpty(usernameField.getText().trim()) || Strings.isNullOrEmpty(passwordField.getText().trim())) return;

        LoginButtonHandler.onClick();

    }

    public Button getPlayButton() {
        return playButton;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }
}
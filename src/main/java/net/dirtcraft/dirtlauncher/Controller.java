package net.dirtcraft.dirtlauncher;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.utils.Utility;
import net.dirtcraft.dirtlauncher.backend.data.LoginButtonHandler;
import net.dirtcraft.dirtlauncher.backend.data.PackCellFactory;

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
    private TextFlow notificationBox;

    @FXML
    private FlowPane loginArea;

    @FXML
    private Pane loginBox;

    @FXML
    private Text headerText;

    public static Controller getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;
        loginArea.setPickOnBounds(false);
        notificationBox.setOpacity(0);
        playButton.setDisable(true);
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        //packs.addAll(PackRegistry.getPacks());
        packList.getStyleClass().add(CssClasses.PACKLIST);
        packList.setCellFactory(useless -> new PackCellFactory());
        packList.setItems(packs);

        WebEngine webEngine = webView.getEngine();

        webEngine.setUserStyleSheetLocation(Utility.getResourcePath(Internal.CSS_HTML, "webEngine.css"));

        webEngine.load("https://dirtcraft.net/");



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

        LoginButtonHandler.onClick();

    }

    @FXML
    private void onKeyTyped(KeyEvent event) {
        if (!PackCellFactory.hasPackSelected) return;
        if (!Utility.isEmptyOrNull(usernameField.getText().trim(), passwordField.getText().trim())) {
            playButton.setDisable(false);
            playButton.setOnAction(action -> LoginButtonHandler.onClick());
        }
        else playButton.setDisable(true);
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

    public TextFlow getNotificationBox() {
        return notificationBox;
    }

    public Pane getLoginBox() {
        return loginBox;
    }
}
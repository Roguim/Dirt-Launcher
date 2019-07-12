package net.dirtcraft.dirtlauncher.Controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.components.LoginButtonHandler;
import net.dirtcraft.dirtlauncher.backend.components.PackCell;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

public class Home {

    private static Home instance;

    @FXML
    private FlowPane packList;

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

    @FXML
    private Button settingsButton;

    @FXML
    private Button settingsButton1;
    @FXML
    private Button settingsButton2;
    @FXML
    private Button settingsButton3;

    public static Home getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;

        // DEBUG BUTTONS
        settingsButton1.setOnMouseClicked(e->LoginButtonHandler.updatePack());
        settingsButton2.setOnMouseClicked(e->LoginButtonHandler.installPack());
        settingsButton3.setOnMouseClicked(e -> DiscordPresence.setDetails("Pressed Button 3"));
        //settingsButton3.setOnMouseClicked(e->LoginButtonHandler.installPack());

        ImageView settingsImage = new ImageView();
        settingsImage.setFitHeight(50);
        settingsImage.setFitWidth(50);
        settingsImage.setImage(MiscUtils.getImage(Internal.ICONS, "settings.png"));
        settingsButton.setGraphic(settingsImage);
        settingsButton.setOnMouseClicked(event -> Settings.getInstance().getStage().show());
        loginArea.setPickOnBounds(false);
        notificationBox.setOpacity(0);
        playButton.setDisable(true);
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.setAll(PackRegistry.getPacks());

        packList.getStyleClass().add(CssClasses.PACKLIST);
        packs.forEach(pack -> packList.getChildren().add(new PackCell(pack)));

        WebEngine webEngine = webView.getEngine();

        webEngine.setUserStyleSheetLocation(MiscUtils.getResourcePath(Internal.CSS_HTML, "webEngine.css"));

        webEngine.load("https://dirtcraft.net/");

        DiscordPresence.initPresence();
        DiscordPresence.setDetails("Selecting a ModPack...");
        DiscordPresence.setState("dirtcraft.net/launcher");

    }

    @FXML
    private void onTabPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB) return;
        if (event.getSource() == usernameField) passwordField.requestFocus();
        else if (event.getSource() == passwordField) usernameField.requestFocus();
    }

    @FXML
    private void onEnterPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) return;
        if (playButton.isDisabled()) return;

        LoginButtonHandler.onClick();
    }

    @FXML
    private void onKeyTyped(KeyEvent event) {
        //if (!PackCellFactory.hasPackSelected) return;
        if (!MiscUtils.isEmptyOrNull(usernameField.getText().trim(), passwordField.getText().trim())) {
            playButton.setDisable(false);
            playButton.setOnAction(e -> LoginButtonHandler.onClick());
        }
        else playButton.setDisable(true);
    }

    public FlowPane getPackList() {
        return packList;
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
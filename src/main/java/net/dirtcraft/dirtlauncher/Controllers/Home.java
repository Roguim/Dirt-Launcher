package net.dirtcraft.dirtlauncher.Controllers;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.utils.Utility;
import net.dirtcraft.dirtlauncher.backend.data.LoginButtonHandler;
import net.dirtcraft.dirtlauncher.backend.data.PackCellFactory;

import java.io.IOException;
import java.util.Arrays;

public class Home {
    private double settingsXOffset;
    private double settingsYOffset;

    private Stage settingsMenu;

    private static Home instance;

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

    @FXML
    private Button settingsButton;

    public static Home getInstance() {
        return instance;
    }

    public Stage getSettingsMenu() {
        return settingsMenu;
    }

    @FXML
    private void initialize() {
        instance = this;

        ImageView settingsImage = new ImageView();
        settingsImage.setFitHeight(50);
        settingsImage.setFitWidth(50);
        settingsImage.setImage(Utility.getImage(Internal.ICONS, "settings.png"));
        settingsButton.setGraphic(settingsImage);
        settingsButton.setOnMouseClicked(e->getSettings());
        loginArea.setPickOnBounds(false);
        notificationBox.setOpacity(0);
        playButton.setDisable(true);
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.setAll(PackRegistry.getPacks());

        packList.getStyleClass().add(CssClasses.PACKLIST);
        packList.setCellFactory(useless -> new PackCellFactory());
        packList.setItems(packs);

        WebEngine webEngine = webView.getEngine();

        webEngine.setUserStyleSheetLocation(Utility.getResourcePath(Internal.CSS_HTML, "webEngine.css"));

        webEngine.load("https://dirtcraft.net/");



    }

    private void getSettings(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Utility.getResourceURL(Internal.SCENES, "settings.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Dirtlauncher Settings");
            stage.setScene(new Scene(root));

            root.setOnMousePressed(event -> {
                settingsXOffset = event.getSceneX();
                settingsYOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - settingsXOffset);
                stage.setY(event.getScreenY() - settingsYOffset);
            });

            settingsMenu = stage;
            stage.show();
        } catch (IOException e){
            System.out.println(String.join(Arrays.toString(e.getStackTrace())));
        }

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
            playButton.setOnAction(e ->LoginButtonHandler.onClick());
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
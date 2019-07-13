package net.dirtcraft.dirtlauncher.Controllers;


import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.components.LoginButtonHandler;
import net.dirtcraft.dirtlauncher.backend.components.PackCell;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
import net.dirtcraft.dirtlauncher.backend.components.PlayButton;

public class Home {

    private static Home instance;

    @FXML
    private VBox packList;

    @FXML
    private WebView webView;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PlayButton playButton;

    @FXML
    private TextFlow notificationBox;

    @FXML
    private FlowPane loginArea;

    @FXML
    private Pane loginBox;

    @FXML
    private Text headerText;

    @FXML
    private ScrollPane packListScroll;

    @FXML
    private Button settingsButton;

    private PackCell activeCell = null;

    public static Home getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;
        ImageView settingsImage = new ImageView();
        settingsImage.setFitHeight(50);
        settingsImage.setFitWidth(50);
        settingsImage.setImage(MiscUtils.getImage(Internal.ICONS, "settings.png"));
        settingsButton.setGraphic(settingsImage);
        settingsButton.setOnMouseClicked(event -> {
            Stage stage = Settings.getInstance().getStage();
            stage.show();
            stage.setOnCloseRequest(e -> {

                JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getMinimumRam().getText())) config.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
                else config.addProperty("minimum-ram", Integer.valueOf(Settings.getInstance().getMinimumRam().getText()));

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getMaximumRam().getText())) config.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
                else config.addProperty("maximum-ram", Integer.valueOf(Settings.getInstance().getMaximumRam().getText()));

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getJavaArguments().getText())) config.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
                else config.addProperty("java-arguments", Settings.getInstance().getJavaArguments().getText());

                FileUtils.writeJsonToFile(Directories.getConfiguration(), config);
            });
        });
        loginArea.setPickOnBounds(false);
        notificationBox.setOpacity(0);
        playButton.setDisable(true);
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.setAll(PackRegistry.getPacks());

        packList.getStyleClass().add(CssClasses.PACKLIST);
        packList.getChildren().clear();
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

    public VBox getPackList() {
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

    public PackCell getActiveCell() {
        return activeCell;
    }

    public void setActiveCell(PackCell activeCell) {
        this.activeCell = activeCell;
    }
}
package net.dirtcraft.dirtlauncher.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import javax.annotation.Nullable;

public class Install {

    private static Install instance;

    private Stage stage;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextFlow notificationText;

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private ProgressBar bottomBar;

    @FXML
    private void initialize() {
        instance = this;

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Stage getStage() {
        return stage;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public ProgressBar getLoadingBar() {
        return loadingBar;
    }

    public ProgressBar getBottomBar() {
        return bottomBar;
    }

    public static Install getInstance() {
        return instance;
    }

    public TextFlow getNotificationText() {
        return notificationText;
    }
}

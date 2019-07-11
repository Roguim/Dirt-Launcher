package net.dirtcraft.dirtlauncher.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;

public class Install {

    private static Install instance;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextFlow textFlow;

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

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public TextFlow getTextFlow() {
        return textFlow;
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

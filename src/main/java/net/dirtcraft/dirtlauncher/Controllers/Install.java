package net.dirtcraft.dirtlauncher.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.LoginButtonHandler;

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
    private FlowPane buttonPane;

    @FXML
    private Button playButton;

    @FXML
    private void initialize() {
        instance = this;

    }

    @FXML
    private void onButtonClick(MouseEvent event) {
        if (!buttonPane.isVisible()) return;
        buttonPane.setVisible(false);
        Stage stage = getStage();
        if (stage != null) stage.close();

        LoginButtonHandler.launchPack(LoginButtonHandler.login());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Stage getStage() {
        return stage;
    }

    public FlowPane getButtonPane() {
        return buttonPane;
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

package net.dirtcraft.dirtlauncher.gui.wizards;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;

import javax.annotation.Nullable;
import java.util.Optional;

public final class Install {

    private static Install instance = null;

    private static Stage stage = null;

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
        playButton.setCursor(Cursor.HAND);

    }

    @FXML
    private void onButtonClick(MouseEvent event) {
        if (!buttonPane.isVisible()) return;
        buttonPane.setVisible(false);
        getStage().ifPresent(Stage::close);

        Optional<Account> account = Main.getAccounts().getSelectedAccount();
        Optional<Pack> pack = Main.getHome().getLoginBar().getActivePackCell();
        if (pack.isPresent() && account.isPresent()){
            pack.get().getModpack().launch();
        }
    }

    public void setStage(Stage stage) {
        Install.stage = stage;
    }

    @Nullable
    public Stage getStageUnsafe() {
        return stage;
    }

    public static Optional<Stage> getStage() {
        if (stage == null || instance == null) return Optional.empty();
        return Optional.of(stage);
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

    public static Optional<Install> getInstance() {
        if (instance == null) return Optional.empty();
        return Optional.of(instance);
    }

    public TextFlow getNotificationText() {
        return notificationText;
    }
}

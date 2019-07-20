package net.dirtcraft.dirtlauncher.Controllers;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.SettingsManager;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;

import java.io.File;
import java.io.IOException;


public class Settings {

    private static Settings instance;

    private static Stage stage;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField minimumRam;

    @FXML
    private TextField maximumRam;

    @FXML
    private TextField javaArguments;

    @FXML
    private TextField gameDirectoryField;

    @FXML
    private Button gameDirectoryButton;

    @FXML
    private void initialize() {
        instance = this;
        SettingsManager config = Main.getSettings();
        gameDirectoryButton.setOnAction(this::onGameDirectoryFolderGuiRequested);
        gameDirectoryField.setText(config.getGameDirectory().toString());
        minimumRam.setText(String.valueOf(config.getMinimumRam()));
        maximumRam.setText(String.valueOf(config.getMaximumRam()));
        javaArguments.setText(config.getJavaArguments());
    }

    public TextField getMinimumRam() {
        return minimumRam;
    }

    public TextField getMaximumRam() {
        return maximumRam;
    }

    public TextField getJavaArguments() {
        return javaArguments;
    }

    public TextField getGameDirectoryField() {
        return gameDirectoryField;
    }

    public static Settings getInstance() {
        return instance;
    }

    private void onGameDirectoryFolderGuiRequested(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(gameDirectoryField.getText()));
        File selectedFolder = directoryChooser.showDialog(stage);
        if (selectedFolder != null) gameDirectoryField.setText(selectedFolder.getPath());
    }

    @FXML
    private void onKeyTyped(KeyEvent event) {
        try {
            Integer.parseInt(event.getCharacter());
        } catch (NumberFormatException exception) {
            event.consume();
            return;
        }
        Object source = event.getSource();
        if (!(source instanceof TextField)) {
            event.consume();
            return;
        }
        TextField textField = (TextField) source;
        if (textField.getSelectedText().length() >= 5) return;
        if (textField.getText().length() >= 5) event.consume();

    }

    @FXML
    private void onClick(MouseEvent event) {
        anchorPane.requestFocus();
    }

    @FXML
    private void onTab(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB) return;
        Object source = event.getSource();
        if (source == minimumRam) maximumRam.requestFocus();
        else if (source == maximumRam) javaArguments.requestFocus();
        else if (source == javaArguments) minimumRam.requestFocus();
        else anchorPane.requestFocus();
    }

    public Stage getStage() {
        return stage;
    }

    public static void loadSettings() {
        try {
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "settings.fxml"));
            Scene scene = new Scene(root, 600, 300);
            Platform.runLater(()->{
                Stage stage = new Stage();
                stage.initOwner(Main.getInstance().getStage());
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initStyle(StageStyle.UTILITY);

                stage.setTitle("Dirt Launcher Settings");
                stage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "settings.png"));
                stage.setScene(scene);
                stage.setResizable(false);
                Settings.stage = stage;
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

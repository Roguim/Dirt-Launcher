package net.dirtcraft.dirtlauncher.Controllers;

import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;

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
    private void initialize() {
        instance = this;

        minimumRam.setPromptText(RamUtils.getMinimumRam() * 1024 + " MB");
        maximumRam.setPromptText(RamUtils.getRecommendedRam() * 1024 + " MB");

        JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

        minimumRam.setText(String.valueOf(config.get("minimum-ram").getAsInt()));
        maximumRam.setText(String.valueOf(config.get("maximum-ram").getAsInt()));
        javaArguments.setText(config.get("java-arguments").getAsString());

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

    public static Settings getInstance() {
        return instance;
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
            root.getStylesheets().add("https://fonts.gstatic.com/s/oleoscript/v7/rax5HieDvtMOe0iICsUccChdu0_y8zac.woff2");
            root.getStylesheets().add("https://fonts.gstatic.com/s/cairo/v5/SLXGc1nY6HkvalIhTpumxdt0.woff2");
            root.getStylesheets().add("https://fonts.gstatic.com/s/russoone/v7/Z9XUDmZRWg6M1LvRYsHOz8mJvLuL9A.woff2");


            Stage stage = new Stage();
            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UTILITY);

            stage.setTitle("Dirt Launcher Settings");
            stage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "settings.png"));
            Scene scene = new Scene(root, 600, 300);
            stage.setScene(scene);
            stage.setResizable(false);

            Settings.stage = stage;
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

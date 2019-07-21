package net.dirtcraft.dirtlauncher.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Constants;
import net.dirtcraft.dirtlauncher.backend.jsonutils.JsonFetcher;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Update {

    private static Update instance;

    private static Stage stage;

    @FXML
    private Button cancel;

    @FXML
    private Button download;

    @FXML
    private void initialize() {
        instance = this;
    }

    @FXML
    private void onClick(MouseEvent event) {
        Object source = event.getSource();
        if (source == download) {
            try {
                Desktop.getDesktop().browse(new URI("https://dirtcraft.net/launcher/download"));
                Platform.exit();
            } catch (URISyntaxException | IOException exception) {
                stage.close();
                exception.printStackTrace();
            }
        } else if (source == cancel) {
            stage.close();
        }
    }

    public static Update getInstance() {
        return instance;
    }

    public static void showStage() {
        try {
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Constants.SCENES, "update.fxml"));
            root.getStylesheets().add("https://fonts.gstatic.com/s/russoone/v7/Z9XUDmZRWg6M1LvRYsHOz8mJvLuL9A.woff2");

            Stage stage = new Stage();
            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UTILITY);

            stage.setTitle("Version " + JsonFetcher.getLatestVersion() + " available");
            stage.getIcons().setAll(MiscUtils.getImage(Constants.ICONS, "update.png"));
            Scene scene = new Scene(root, 400, 200);
            stage.setScene(scene);
            stage.setResizable(false);

            stage.show();

            Update.stage = stage;

        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    public static boolean hasUpdate() throws IOException {
        return !JsonFetcher.getLatestVersion().equalsIgnoreCase(Constants.LAUNCHER_VERSION);
    }

}

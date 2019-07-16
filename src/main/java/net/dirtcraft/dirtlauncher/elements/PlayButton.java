package net.dirtcraft.dirtlauncher.elements;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Controllers.Install;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.game.DownloadManager;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.io.IOException;
import java.util.Collections;

public class PlayButton extends Button {
    private Types type;
    private Pack pack;
    private LoginBar loginBar;

    public PlayButton(LoginBar loginBar){
        this.loginBar = loginBar;
        setFocusTraversable(false);
        setId("PlayButton");
        setCursor(Cursor.HAND);
    }

    public void setType(Types type, Pack pack) {
        this.type = type;
        this.pack = pack;
        setText(type.toString());
    }

    @Override
    public void fire() {
        Account account = loginBar.login();
        if (account != null)
            switch (type) {
                case INSTALL:
                    installPack(pack);
                    return;
                case UPDATE:
                    updatePack(pack);
                    return;
                case PLAY:
                    launchPack(account, pack);
                    return;
                default:
                    NotificationHandler.displayError(null, pack);
                    return;
            }
    }
    public enum Types{
        INSTALL,
        UPDATE,
        PLAY,
        INITIAL;

        @Override
        public String toString() {
            switch (this) {
                case PLAY:
                    return "Play";
                case UPDATE:
                    return "Update";
                default:
                case INSTALL:
                    return "Install";
                case INITIAL:
                    return "N/A";
            }
        }
    }

    public void launchPack(Account account, Pack modPack) {
        LaunchGame.isGameRunning = true;
        LaunchGame.loadServerList(modPack);
        LaunchGame.launchPack(modPack, account);
    }

    private void updatePack(Pack modPack){
        if (Internal.VERBOSE) {
            System.out.println("Updated the game");
        }
        launchInstallScene(modPack);
        new Thread(() -> {
            try {
                DownloadManager.completePackSetup(modPack, Collections.emptyList(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void installPack(Pack modPack) {
        if (Internal.VERBOSE) {
            System.out.println("Installing the pack");
        }

        launchInstallScene(modPack);
        new Thread(() -> {
            try {
                DownloadManager.completePackSetup(modPack, Collections.emptyList(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void launchInstallScene(Pack modPack) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Installing " + modPack.getName() + "...");
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "install.fxml"));
            root.getStylesheets().add("https://fonts.gstatic.com/s/russoone/v7/Z9XUDmZRWg6M1LvRYsHOz8mJvLuL9A.woff2");


            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "install.png"));

            stage.setScene(new Scene(root, MiscUtils.screenDimension.getWidth() / 3, MiscUtils.screenDimension.getHeight() / 4));
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);

            stage.show();

            Install.getInstance().ifPresent(install -> {
                TextFlow notificationArea = install.getNotificationText();
                Text notification = new Text("Beginning Download...");
                notification.setFill(Color.WHITE);
                notification.setTextOrigin(VPos.CENTER);
                notification.setTextAlignment(TextAlignment.CENTER);
                notificationArea.getChildren().add(notification);

                notification.setText("Preparing To Install...");
                install.setStage(stage);
            });


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

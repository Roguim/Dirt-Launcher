package net.dirtcraft.dirtlauncher.nodes.loginarea;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cydhra.nidhogg.data.Session;
import net.dirtcraft.dirtlauncher.nodes.Pack;
import net.dirtcraft.dirtlauncher.stages.Home;
import net.dirtcraft.dirtlauncher.stages.Install;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.game.DownloadManager;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public final class PlayButton extends Button {
    private Types type;
    private Pack pack;
    private LoginBar loginBar;

    public PlayButton(LoginBar loginBar){
        this.type = Types.INITIAL;
        setTextOverrun(OverrunStyle.CLIP);
        this.loginBar = loginBar;
        setFocusTraversable(false);
        setId("PlayButton");
        setCursor(Cursor.HAND);
    }

    public void setType(Session session) {
        if (session == null || type != Types.PLAY) setText(type.toString());
        else setText(type.toString() + " As " + session.getAlias());
    }

    public void setType(Types type, Pack pack) {
        Optional<Session> account = Main.getAccounts().getSelectedAccount();
        this.type = type;
        this.pack = pack;
        if (!account.isPresent() || type != Types.PLAY) setText(type.toString());
        else setText(type.toString() + " As " + account.get().getAlias());
    }

    @Override
    public void fire() {
        Optional<Session> session = Main.getAccounts().getValidSelectedAccount();
        if (session.isPresent())
            switch (type) {
                case INSTALL:
                    installPack(pack);
                    return;
                case UPDATE:
                    updatePack(pack);
                    return;
                case PLAY:
                    launchPack(session.get(), pack);
                    return;
                default:
                    Home.getInstance().getNotificationBox().displayError(null, pack);
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
                    return "Please Select A Pack";
            }
        }
    }

    public void setPack(Pack pack){
        this.pack = pack;
    }

    public void launchPack(Session session, Pack modPack) {
        LaunchGame.isGameRunning = true;
        LaunchGame.loadServerList(modPack);
        LaunchGame.launchPack(modPack, session);
    }

    private void updatePack(Pack modPack){
        if (Constants.VERBOSE) {
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

    public void installPack(Pack modPack) {
        if (Constants.VERBOSE) {
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
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Constants.JAR_SCENES, "install.fxml"));
            root.getStylesheets().add("https://fonts.gstatic.com/s/russoone/v7/Z9XUDmZRWg6M1LvRYsHOz8mJvLuL9A.woff2");

            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "install.png"));

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

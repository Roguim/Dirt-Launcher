package net.dirtcraft.dirtlauncher.gui.home.login;

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
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackSelector;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.IOException;
import java.util.Optional;

public final class ActionButton extends Button {
    private Types type;
    private PackSelector pack;

    public ActionButton(){
        this.type = Types.INITIAL;
        setTextOverrun(OverrunStyle.CLIP);
        setFocusTraversable(false);
        setId("PlayButton");
        setCursor(Cursor.HAND);
    }

    public void setType(Account session) {
        if (session == null || type != Types.PLAY) setText(type.toString());
        else setText(type.toString() + " As " + session.getAlias());
    }

    public void setType(Types type, PackSelector pack) {
        Optional<Account> account = Main.getAccounts().getSelectedAccount();
        this.type = type;
        this.pack = pack;
        if (!account.isPresent() || type != Types.PLAY) setText(type.toString());
        else setText(type.toString() + " As " + account.get().getAlias());
    }

    public void refresh(){
        type = Types.fromPack(pack);
    }

    @Override
    public void fire() {
        if (Main.getAccounts().hasSelectedAccount())
            switch (type) {
                case INSTALL:
                    launchInstallScene(pack);
                    pack.getModpack().install().thenRun(pack::update);
                    return;
                case UPDATE:
                    launchInstallScene(pack);
                    pack.getModpack().update().thenRun(pack::update);
                    return;
                case PLAY:
                    launchInstallScene(pack);
                    pack.getModpack().launch();
                    return;
                default:
                    Main.getHome().getNotificationBox().displayError(null);
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

        public static Types fromPack(PackSelector selected){
            if (selected == null) return INITIAL;
            Modpack modpack = selected.getModpack();
            if (!modpack.isInstalled()) return INSTALL;
            if (modpack.isOutdated()) return UPDATE;
            if (modpack.isInstalled()) return PLAY;
            return INSTALL;
        }
    }

    public void setPack(PackSelector pack){
        this.pack = pack;
    }

    private void launchInstallScene(PackSelector modPack) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Installing " + modPack.getModpack().getName() + "...");
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Constants.JAR_SCENES, "install.fxml"));

            stage.initOwner(Main.getHome().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "install.png"));

            stage.setScene(new Scene(root, Main.screenDimension.getWidth() / 3, Main.screenDimension.getHeight() / 4));
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

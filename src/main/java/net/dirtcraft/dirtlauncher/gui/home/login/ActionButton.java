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
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.game.authentification.Account;
import net.dirtcraft.dirtlauncher.game.modpacks.Modpack;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackSelector;
import net.dirtcraft.dirtlauncher.gui.wizards.Install;
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
                case REPAIR:
                    MiscUtils.launchInstallScene(pack);
                    pack.getModpack().install().thenRun(pack::update);
                    return;
                case UPDATE:
                    MiscUtils.launchInstallScene(pack);
                    pack.getModpack().update().thenRun(pack::update);
                    return;
                case PLAY:
                    MiscUtils.launchInstallScene(pack);
                    pack.getModpack().launch();
                    return;
                default:
                    Main.getHome().getNotificationBox().displayError(null);
            }
    }
    public enum Types{
        INSTALL,
        REPAIR,
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
                case REPAIR:
                    return "Fix Dependencies";
                case INSTALL:
                    return "Install";
                default:
                case INITIAL:
                    return "Please Select A Pack";
            }
        }

        public static Types fromPack(PackSelector selected){
            if (selected == null) return INITIAL;
            Modpack modpack = selected.getModpack();
            if (!modpack.isInstalled()) return INSTALL;
            if (!modpack.isDependantsInstalled()) return REPAIR;
            if (modpack.isOutdated()) return UPDATE;
            if (modpack.isInstalled()) return PLAY;
            return INSTALL;
        }
    }

    public void setPack(PackSelector pack){
        this.pack = pack;
    }
}

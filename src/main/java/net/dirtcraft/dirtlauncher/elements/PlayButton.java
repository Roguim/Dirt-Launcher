package net.dirtcraft.dirtlauncher.elements;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;

public class PlayButton extends Button {
    private Types type;
    private Pack pack;

    public PlayButton(){
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
        Account account = LoginButtonHandler.login();
        if (account != null)
            switch (type) {
                case INSTALL:
                    LoginButtonHandler.installPack(pack);
                    return;
                case UPDATE:
                    LoginButtonHandler.updatePack(pack);
                    return;
                case PLAY:
                    LoginButtonHandler.launchPack(account, pack);
                    return;
                default:
                    LoginButtonHandler.displayError(null, pack);
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
}

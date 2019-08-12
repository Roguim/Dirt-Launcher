package net.dirtcraft.dirtlauncher.gui.home.login;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

final class LogoutButton extends Button {
    private final LoginBar loginBar;
    LogoutButton(LoginBar loginBar){
        this.loginBar = loginBar;
        javafx.scene.image.Image logoutImage = new Image(MiscUtils.getResourceStream(Constants.JAR_ICONS, "logout.png"));
        ImageView logoutImgage = new ImageView(logoutImage);
        logoutImgage.resize(20,20);
        logoutImgage.setFitWidth(25);
        logoutImgage.setFitHeight(25);
        setGraphic(logoutImgage);
        setId("LogoutButton");
        setFocusTraversable(false);
    }

    @Override
    public void fire() {
        loginBar.logOut();
    }
}

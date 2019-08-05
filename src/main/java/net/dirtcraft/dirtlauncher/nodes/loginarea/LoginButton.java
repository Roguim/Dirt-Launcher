package net.dirtcraft.dirtlauncher.nodes.loginarea;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;

final class LoginButton extends Button {
    private LoginBar loginBar;

    LoginButton(LoginBar loginBar){
        setMaxSize(58,59);
        setMinSize(58,59);
        setText("Login");
        setTextOverrun(OverrunStyle.CLIP);
        this.loginBar = loginBar;
        setFocusTraversable(false);
        setId("PlayButton");
        setCursor(Cursor.HAND);
    }

    @Override
    public void fire() {
        loginBar.login();
    }
}

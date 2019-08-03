package net.dirtcraft.dirtlauncher.nodes;

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
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.game.DownloadManager;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.stages.Home;
import net.dirtcraft.dirtlauncher.stages.Install;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public final class LoginButton extends Button {
    private LoginBar loginBar;

    public LoginButton(LoginBar loginBar){
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

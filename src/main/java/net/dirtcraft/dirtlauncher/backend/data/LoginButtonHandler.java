package net.dirtcraft.dirtlauncher.backend.data;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import net.dirtcraft.dirtlauncher.Controller;

import javax.swing.*;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Button playButton;

    public static void onClick(ActionEvent event){
        if (!initialized){
            usernameField = Controller.getInstance().getUsernameField();
            passwordField = Controller.getInstance().getPasswordField();
            playButton = Controller.getInstance().getPlayButton();
        }
        String user = usernameField.getText();
        String pass = passwordField.getText();

        System.out.println(user);
        System.out.println(pass);
    }
}

package net.dirtcraft.dirtlauncher.backend.data;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.dirtcraft.dirtlauncher.Controller;
import net.dirtcraft.dirtlauncher.backend.Utils.Verification;
import net.dirtcraft.dirtlauncher.backend.objects.Account;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Button playButton;
    private static Thread uiCallback;
    private static TextFlow messageBox;

    public static Account onClick() {
        if (!initialized){
            usernameField = Controller.getInstance().getUsernameField();
            passwordField = Controller.getInstance().getPasswordField();
            playButton = Controller.getInstance().getPlayButton();
            messageBox = Controller.getInstance().getMessageBox();
            uiCallback = null;
        }
        Account userAccount = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            userAccount = Verification.login(email, password);
        } catch (InvalidCredentialsException e) {
            displayLoginError("Please enter a valid email / password.");
        } catch (IllegalArgumentException e){
            displayLoginError("Your email or password was invalid.");
        }

        //TODO do stuff with the userAccount

        return userAccount;
    }

    public static void displayLoginError(String exception){
        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("errorMessage");
        text.setFill(Paint.valueOf("WHITE"));
        text.setTextOrigin(VPos.CENTER);
        text.setText(exception);

        messageBox.setTextAlignment(TextAlignment.CENTER);
        messageBox.getChildren().clear();
        messageBox.getChildren().add(text);

        uiCallback = new Thread(() -> {
            Platform.runLater(() -> messageBox.setOpacity(1));
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> messageBox.setOpacity(0));
                Platform.runLater(()-> uiCallback = null);
            } catch (InterruptedException ex) {
                //we interupted this boi so who cares
            }

        });
        uiCallback.start();
    }
}

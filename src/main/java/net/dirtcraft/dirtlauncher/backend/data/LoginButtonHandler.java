package net.dirtcraft.dirtlauncher.backend.data;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controller;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.Utils.Verification;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginResult;

import javax.annotation.Nullable;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Button playButton;
    private static Thread uiCallback;
    private static TextFlow messageBox;
    private static Pane launchBox;

    @Nullable
    public static Account onClick() {
        if (!initialized){
            usernameField = Controller.getInstance().getUsernameField();
            passwordField = Controller.getInstance().getPasswordField();
            playButton = Controller.getInstance().getPlayButton();
            messageBox = Controller.getInstance().getMessageBox();
            launchBox = Controller.getInstance().getLaunchBox();
            uiCallback = null;
        }
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            account = Verification.login(email, password);
            displayLoginError(account, LoginResult.SUCCESS);
            //} catch (Exception e){
        } catch (InvalidCredentialsException e) {
                displayLoginError(account, LoginResult.INVALID_CREDENTIALS);
        } catch (IllegalArgumentException e) {
            displayLoginError(account, LoginResult.ILLEGAL_ARGUMENT);
        } catch (UserMigratedException e) {
            displayLoginError(account, LoginResult.USER_MIGRATED);
        }

        //TODO - do stuff with the userAccount

        return account;
    }

    private static void displayLoginError(Account account, LoginResult result){

        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("errorMessage");
        text.setFill(Color.WHITE);

        /* Help! Text isn't being aligned correctly */
        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);
        /* Help! Text isn't being aligned correctly */

        if (account != null && result == LoginResult.SUCCESS) {
            text.setText("Successfully logged into " + account.getUsername() + "'s account");
        }
        else {
            ShakeTransition animation = new ShakeTransition(messageBox);
            animation.playFromStart();

            switch (result) {
                case USER_MIGRATED:
                    text.setText("Please use your E-Mail to log in!");
                    break;
                case ILLEGAL_ARGUMENT:
                    text.setText("Your username or password contains invalid arguments!");
                    break;
                default:
                case INVALID_CREDENTIALS:
                    text.setText("Your E-Mail or password is invalid!");
                    break;
            }

        }

        if (messageBox.getTextAlignment() != TextAlignment.CENTER) messageBox.setTextAlignment(TextAlignment.CENTER);

        if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
        messageBox.getChildren().setAll(text);


        uiCallback = getThread(result);

        uiCallback.start();

    }

    private static Thread getThread(LoginResult result) {
        return new Thread(() -> {
            Platform.runLater(() -> {
                if (messageBox.getOpacity() != 1) messageBox.setOpacity(1);
            });
            try {
                Thread.sleep((result == LoginResult.SUCCESS ? 2 : 5) * 1000);
                Platform.runLater(() -> {
                    if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
                    if (result == LoginResult.SUCCESS) Main.getInstance().getStage().close();
                    if (uiCallback != null) uiCallback = null;
                });
            } catch (InterruptedException ex) {}
        });
    }
}

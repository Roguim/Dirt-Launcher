package net.dirtcraft.dirtlauncher.backend.data;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controller;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.Verification;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginResult;

import javax.annotation.Nullable;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Thread uiCallback;
    private static TextFlow messageBox;
    private static Button playButton;
    private static PackAction packAction;
    private static Pack modPack;

    private static void Initialize(){
        usernameField = Controller.getInstance().getUsernameField();
        passwordField = Controller.getInstance().getPasswordField();
        messageBox = Controller.getInstance().getNotificationBox();
        playButton = Controller.getInstance().getPlayButton();
        initialized = true;
        uiCallback = null;
        packAction = null;
    }

    @Nullable
    public static void onClick() {
        if (!initialized) Initialize();
        switch (packAction){
            case PLAY: launchPack(); return;
            case UPDATE: updatePack(); return;
            case INSTALL: installPack(); return;
            default:displayNotification(null, LoginResult.ILLEGAL_ARGUMENT);
        }
    }

    public static void launchPack() {
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            account = Verification.login(email, password);
            displayNotification(account, LoginResult.SUCCESS);
        } catch (InvalidCredentialsException e) {
            displayNotification(account, LoginResult.INVALID_CREDENTIALS);
        } catch (IllegalArgumentException e) {
            displayNotification(account, LoginResult.ILLEGAL_ARGUMENT);
        } catch (UserMigratedException e) {
            displayNotification(account, LoginResult.USER_MIGRATED);
        }
    }

    public static void updatePack(){
        System.out.println("Updated the game");
    }

    public static void installPack(){
        System.out.println("Installed the game");
    }

    private static void displayNotification(Account account, LoginResult result){

        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
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

    public static void setAction(PackAction action, Pack pack){
        if (!initialized) Initialize();
        modPack = pack;
        packAction = action;
        playButton.setText(action.toString());
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
            } catch (InterruptedException ignored) {}
        });
    }
}

package net.dirtcraft.dirtlauncher.backend.data;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.Pack;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginResult;
import net.dirtcraft.dirtlauncher.backend.utils.Utility;
import net.dirtcraft.dirtlauncher.backend.utils.Verification;

import javax.annotation.Nullable;
import java.io.IOException;

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
        usernameField = Home.getInstance().getUsernameField();
        passwordField = Home.getInstance().getPasswordField();
        messageBox = Home.getInstance().getNotificationBox();
        playButton = Home.getInstance().getPlayButton();
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
            default: displayNotification(null);
        }
    }

    public static void launchPack() {
        Account account = login();
        if (account == null) {
            displayNotification(null);
            return;
        }

        /*
        LAUNCH PACK STUFF HERE
         */
    }

    public static void updatePack(){
        System.out.println("Updated the game");

        /*
        UPDATE PACK STUFF HERE
         */
    }

    public static void installPack(){
        System.out.println("Installed the game");

        /*
        INSTALL PACK STUFF HERE
         */

    }

    @Nullable
    private static Account login() {
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            account = Verification.login(email, password);
            displayNotification(LoginResult.SUCCESS);
        } catch (InvalidCredentialsException e) {
            displayNotification(LoginResult.INVALID_CREDENTIALS);
        } catch (IllegalArgumentException e) {
            displayNotification(LoginResult.ILLEGAL_ARGUMENT);
        } catch (UserMigratedException e) {
            displayNotification(LoginResult.USER_MIGRATED);
        }

        return account;
    }

    private static void displayNotification(LoginResult result) {

        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
        text.setFill(Color.WHITE);

        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        ShakeTransition animation = new ShakeTransition(messageBox);
        animation.playFromStart();

        switch (result) {
            case USER_MIGRATED:
                text.setText("Please use your E-Mail to log in!");
                break;
            case ILLEGAL_ARGUMENT:
                text.setText("Your username or password contains invalid arguments!");
                break;
            case INVALID_CREDENTIALS:
                text.setText("Your E-Mail or password is invalid!");
                break;
            default:
                text.setText("Your ModPack installation is corrupted!");
                break;
        }


        if (messageBox.getTextAlignment() != TextAlignment.CENTER) messageBox.setTextAlignment(TextAlignment.CENTER);

        if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
        messageBox.getChildren().setAll(text);


        uiCallback = getThread();

        uiCallback.start();

    }

    public static void setAction(PackAction action, Pack pack){
        if (!initialized) Initialize();
        modPack = pack;
        packAction = action;
        playButton.setText(action.toString());
    }

    private static Thread getThread() {
        return new Thread(() -> {
            Platform.runLater(() -> {
                if (messageBox.getOpacity() != 1) messageBox.setOpacity(1);
            });
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
                    if (uiCallback != null) uiCallback = null;
                });
            } catch (InterruptedException ignored) {
            }
        });
    }

    private static void launchInstallScene() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Install");
            Parent root = FXMLLoader.load(Utility.getResourceURL(Internal.SCENES, "popup.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

package net.dirtcraft.dirtlauncher.backend.data;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    @Nullable
    public static Account onClick() {
        if (!initialized){
            usernameField = Home.getInstance().getUsernameField();
            passwordField = Home.getInstance().getPasswordField();
            messageBox = Home.getInstance().getNotificationBox();
            initialized = true;
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

    private static Thread getThread(LoginResult result) {
        return new Thread(() -> {
            Platform.runLater(() -> {
                if (messageBox.getOpacity() != 1) messageBox.setOpacity(1);
            });
            try {
                Thread.sleep((result == LoginResult.SUCCESS ? 2 : 5) * 1000);
                Platform.runLater(() -> {
                    if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
                    if (result == LoginResult.SUCCESS) {
                        /* DO STUFF ONCE LOGGED IN HERE */
                        //Main.getInstance().getStage().close();
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
                    if (uiCallback != null) uiCallback = null;
                });
            } catch (InterruptedException ignored) {}
        });
    }
}

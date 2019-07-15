package net.dirtcraft.dirtlauncher.elements;

import javafx.application.Platform;
import javafx.event.Event;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cydhra.nidhogg.exception.InvalidCredentialsException;
import net.cydhra.nidhogg.exception.UserMigratedException;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.Controllers.Install;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.ShakeTransition;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.game.DownloadManager;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginError;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.Verification;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;

public class LoginButtonHandler {
    private static boolean initialized = false;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Thread uiCallback;
    private static TextFlow messageBox;

    private static void Initialize(){
        LoginBar loginBar = Home.getInstance().getLoginBar();
        usernameField = loginBar.getUsernameField();
        passwordField = loginBar.getPassField();
        messageBox = Home.getInstance().getNotificationBox();
        initialized = true;
        uiCallback = null;
    }

    public static void launchPack(Account account, Pack modPack) {
        if (!initialized) Initialize();
        LaunchGame.launchPack(modPack, account);
    }

    public static void updatePack(Pack modPack){
        if (!initialized) Initialize();
        if (Internal.VERBOSE) {
            System.out.println("Updated the game");
        }
        launchInstallScene(modPack);
        new Thread(() -> {
            try {
                DownloadManager.completePackSetup(modPack, Collections.emptyList(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void installPack(Pack modPack) {
        if (!initialized) Initialize();
        if (Internal.VERBOSE) {
            System.out.println("Installing the pack");
        }

        launchInstallScene(modPack);
        new Thread(() -> {
            try {
                DownloadManager.completePackSetup(modPack, Collections.emptyList(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Nullable
    public static Account login() {
        if (!initialized) Initialize();
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            account = Verification.login(email, password);
        } catch (InvalidCredentialsException e) {
            displayError(LoginError.INVALID_CREDENTIALS, null);
        } catch (IllegalArgumentException e) {
            displayError(LoginError.ILLEGAL_ARGUMENT, null);
        } catch (UserMigratedException e) {
            displayError(LoginError.USER_MIGRATED, null);
        }

        return account;
    }

    public static void displayError(LoginError result, Pack modPack) {
        if (!initialized) Initialize();
        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
        text.setFill(Color.WHITE);

        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        ShakeTransition animation = new ShakeTransition(messageBox);
        animation.playFromStart();

        if (result == null) text.setText("Your " + modPack.getName() + " Installation Is Corrupted!");
        else switch (result) {
            case USER_MIGRATED:
                text.setText("Please use your E-Mail to log in!");
                break;
            case ILLEGAL_ARGUMENT:
                text.setText("Your username or password contains invalid arguments!");
                break;
            case INVALID_CREDENTIALS:
                text.setText("Your E-Mail or password is invalid!");
                break;

        }


        if (messageBox.getTextAlignment() != TextAlignment.CENTER) messageBox.setTextAlignment(TextAlignment.CENTER);

        if (messageBox.getOpacity() != 0) messageBox.setOpacity(0);
        messageBox.getChildren().setAll(text);

        uiCallback = getThread();

        uiCallback.start();

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
            } catch (InterruptedException ignored) { }
        });
    }

    private static void launchInstallScene(Pack modPack) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Installing " + modPack.getName() + "...");
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "install.fxml"));
            root.getStylesheets().add("https://fonts.gstatic.com/s/russoone/v7/Z9XUDmZRWg6M1LvRYsHOz8mJvLuL9A.woff2");


            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "install.png"));

            stage.setScene(new Scene(root, MiscUtils.screenDimension.getWidth() / 3, MiscUtils.screenDimension.getHeight() / 4));
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);

            stage.show();

            Install.getInstance().ifPresent(install -> {
                TextFlow notificationArea = install.getNotificationText();
                Text notification = new Text("Beginning Download...");
                notification.setFill(Color.WHITE);
                notification.setTextOrigin(VPos.CENTER);
                notification.setTextAlignment(TextAlignment.CENTER);
                notificationArea.getChildren().add(notification);

                notification.setText("Preparing To Install...");
                install.setStage(stage);
            });


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

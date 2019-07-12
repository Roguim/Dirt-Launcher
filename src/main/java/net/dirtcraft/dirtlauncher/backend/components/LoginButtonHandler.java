package net.dirtcraft.dirtlauncher.backend.components;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
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
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.installation.DownloadManager;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.objects.Account;
import net.dirtcraft.dirtlauncher.backend.objects.LoginResult;
import net.dirtcraft.dirtlauncher.backend.objects.PackAction;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.Verification;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

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

    public static void onClick() {
        if (!initialized) Initialize();
        Account account = login();
        if (account == null) return;
        else switch (packAction) {
            case INSTALL:
                installPack();
                return;
            case UPDATE:
                updatePack();
                return;
            case PLAY:
                launchPack(account);
                return;
            default:
                displayError(null);
                return;
        }
    }

    public static void launchPack(Account account) {

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

    public static void installPack() {
        System.out.println("Installing the pack");

        launchInstallScene();
        new Thread(() -> {
            try {
                DownloadManager.completePackSetup(modPack, Arrays.asList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Nullable
    private static Account login() {
        Account account = null;

        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            account = Verification.login(email, password);
        } catch (InvalidCredentialsException e) {
            displayError(LoginResult.INVALID_CREDENTIALS);
        } catch (IllegalArgumentException e) {
            displayError(LoginResult.ILLEGAL_ARGUMENT);
        } catch (UserMigratedException e) {
            displayError(LoginResult.USER_MIGRATED);
        }

        return account;
    }

    private static void displayError(LoginResult result) {

        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
        text.setFill(Color.WHITE);

        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        ShakeTransition animation = new ShakeTransition(messageBox);
        animation.playFromStart();

        if (result == null) text.setText("Your " + modPack.getName() + " installation is corrupted!");
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
            } catch (InterruptedException ignored) { }
        });
    }

    private static void launchInstallScene() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Installing " + modPack.getName() + "...");
            Parent root = FXMLLoader.load(MiscUtils.getResourceURL(Internal.SCENES, "install.fxml"));


            stage.initOwner(Main.getInstance().getStage());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            stage.getIcons().setAll(MiscUtils.getImage(Internal.ICONS, "install.png"));

            stage.setScene(new Scene(root, MiscUtils.screenDimension.getWidth() / 3, MiscUtils.screenDimension.getHeight() / 4));
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);

            stage.show();

            TextFlow notificationArea = Install.getInstance().getNotificationText();
            Text notification = new Text("Beginning download!");
            notification.setFill(Color.WHITE);
            notification.setTextOrigin(VPos.CENTER);
            notification.setTextAlignment(TextAlignment.CENTER);
            notificationArea.getChildren().add(notification);

            notification.setText("Preparing to install...");

            //TODO: @TECHDG
            //This is the bar in the middle
            ProgressBar loadingBar = Install.getInstance().getLoadingBar();

            //This is the bar on the bottom which tracks the overrall progress
            ProgressBar bottomBar = Install.getInstance().getBottomBar();


        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

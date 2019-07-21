package net.dirtcraft.dirtlauncher.elements;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.components.ShakeTransition;
import net.dirtcraft.dirtlauncher.backend.objects.LoginError;

public class NotificationHandler {
    private static boolean initialized = false;
    private static Thread uiCallback;
    private static TextFlow messageBox;

    private static void Initialize(){
        messageBox = Home.getInstance().getNotificationBox();
        initialized = true;
        uiCallback = null;
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
}

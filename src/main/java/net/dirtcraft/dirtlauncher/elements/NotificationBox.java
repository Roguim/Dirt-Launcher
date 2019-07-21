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

public class NotificationBox extends TextFlow{
    private boolean initialized = false;
    private Thread uiCallback;

    private void Initialize(){
        initialized = true;
        uiCallback = null;
    }

    public void displayError(LoginError result, Pack modPack) {
        if (!initialized) Initialize();
        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
        text.setFill(Color.WHITE);

        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        ShakeTransition animation = new ShakeTransition(this);
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
        if (getTextAlignment() != TextAlignment.CENTER) setTextAlignment(TextAlignment.CENTER);
        if (getOpacity() != 0) setOpacity(0);
        getChildren().setAll(text);
        uiCallback = getThread();
        uiCallback.start();
    }

    private Thread getThread() {
        return new Thread(() -> {
            Platform.runLater(() -> {
                if (getOpacity() != 1) setOpacity(1);
            });
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    if (getOpacity() != 0) setOpacity(0);
                    if (uiCallback != null) uiCallback = null;
                });
            } catch (InterruptedException ignored) { }
        });
    }
}

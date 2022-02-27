package net.dirtcraft.dirtlauncher.gui.home.login;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.dirtcraft.dirtlauncher.game.authentification.LoginError;
import net.dirtcraft.dirtlauncher.gui.components.ShakeTransition;

public final class NotificationBox extends TextFlow{
    private boolean initialized = false;
    private Thread uiCallback;

    public NotificationBox(){
        setOpacity(0);
    }

    private void Initialize(){
        initialized = true;
        uiCallback = null;
    }

    public void displayError(LoginError result) {
        if (!initialized) Initialize();
        if (uiCallback != null) uiCallback.interrupt();

        Text text = new Text();
        text.getStyleClass().add("NotificationText");
        text.setFill(Color.WHITE);

        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        ShakeTransition animation = new ShakeTransition(this);
        animation.playFromStart();

        text.setText(result.toString());
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

package net.dirtcraft.dirtlauncher.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.elements.NotificationBox;
import net.dirtcraft.dirtlauncher.elements.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.elements.LoginBar;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.net.URI;

public final class Home {
    private static Home instance;

    @FXML
    private Pane extraPane;

    @FXML
    private StackPane webArea;

    @FXML
    private VBox packList;

    @FXML
    private NotificationBox notificationBox;

    @FXML
    private Button settingsButton;

    @FXML
    private LoginBar loginBar;
    private PasswordField passwordField;
    private TextField usernameField;
    private Button playButton;

    @FXML   //this is all async
    private void initialize() {
        updatePacksAsync();
        Platform.runLater(this::initWebView);
        instance = this;
        notificationBox.setOpacity(0);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                          //
        //                                              SETTINGS INIT                                               //
        //                                                                                                          //
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        settingsButton.setOnMouseClicked(event -> {
            if (!Constants.VERBOSE) extraPane.setDisable(true);
            Stage stage = net.dirtcraft.dirtlauncher.Controllers.Settings.getInstance().getStage();
            stage.show();
            stage.setOnCloseRequest(e -> {
                final int minimumRam = Integer.valueOf(Settings.getInstance().getMinimumRam().getText());
                final int maximumRam = Integer.valueOf(Settings.getInstance().getMaximumRam().getText());
                final String gameDirectory = Settings.getInstance().getGameDirectoryField().getText();
                final String javaArguments = Settings.getInstance().getJavaArguments().getText();
                Main.getSettings().updateSettings(minimumRam, maximumRam, javaArguments, gameDirectory);
            });
        });

        //                  DISCORD PRESENCE INIT
        DiscordPresence.initPresence();
        DiscordPresence.setDetails("Selecting a ModPack...");
        DiscordPresence.setState("www.dirtcraft.net");

        //                  LOGIN BAR INIT
        passwordField = loginBar.getPassField();
        usernameField = loginBar.getUsernameField();
        playButton = loginBar.getActionButton();
        usernameField.setOnKeyTyped(this::setKeyTypedEvent);
        passwordField.setOnKeyPressed(this::setKeyTypedEvent);
    }

    private void initWebView(){
        WebView webView = new WebView();
        webView.setFocusTraversable(false);
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserStyleSheetLocation(MiscUtils.getResourcePath(Constants.JAR_CSS_HTML, "webEngine.css"));
        webEngine.load("https://dirtcraft.net/launcher/");
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (!(newValue == Worker.State.SUCCEEDED)) return;
            EventListener listener = e -> {
                HTMLAnchorElement element = (HTMLAnchorElement) e.getTarget();
                try {
                    Desktop.getDesktop().browse(new URI(element.getHref()));
                    if (e.getCancelable()){
                        e.preventDefault();
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            };
            Document doc = webEngine.getDocument();
            NodeList lista = doc.getElementsByTagName("a");
            for (int i = 0; i < lista.getLength(); i++) {
                if (!(lista.item(i) instanceof HTMLAnchorElement)) continue;
                ((EventTarget) lista.item(i)).addEventListener("click", listener, false);
            }
        });
        webArea.getChildren().add(webView);
    }

    private void updatePacksAsync(){
        new Thread(()-> {
            ObservableList<Pack> packs = FXCollections.observableArrayList();
            packs.addAll(PackRegistry.getPacks());
            packList.getStyleClass().add(Constants.CSS_CLASS_PACKLIST);
            Platform.runLater(() -> {
                packList.getChildren().clear();
                packList.getChildren().addAll(packs);
            });
            if (Constants.VERBOSE) System.out.println("Packlist built!");
        }).start();
    }

    private void setKeyTypedEvent(KeyEvent event) {
        if (!loginBar.getActivePackCell().isPresent()) {
            playButton.setDisable(true);
            return;
        }

        if (!MiscUtils.isEmptyOrNull(usernameField.getText().trim(), passwordField.getText().trim()) || loginBar.hasAccount()) {
            playButton.setDisable(false);
            playButton.setOnAction(e -> loginBar.getActionButton().fire());
        } else playButton.setDisable(true);
    }

    public NotificationBox getNotificationBox() {
        return notificationBox;
    }

    public LoginBar getLoginBar() {
        return loginBar;
    }

    public static Home getInstance() {
        return instance;
    }

}
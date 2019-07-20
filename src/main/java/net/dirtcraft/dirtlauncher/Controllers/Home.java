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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.elements.LoginBar;
import net.dirtcraft.dirtlauncher.elements.PackCell;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public class Home {
    private static Home instance;

    @FXML
    private StackPane webArea;

    @FXML
    private VBox packList;

    @FXML
    private TextFlow notificationBox;

    @FXML
    private Button settingsButton;

    @FXML
    private LoginBar loginBar;
    private PasswordField passwordField;
    private TextField usernameField;
    private Button playButton;
    private Logger logger;

    public Home(){
        new Thread(()->{
            logger = null;
            while (Main.getLogger() == null){
                try{
                    Thread.sleep(5);
                } catch (Exception ignored){}
            }
            logger = Main.getLogger();
        });
    }

    public static Home getInstance() {
        return instance;
    }

    @FXML   //this is all async
    private void initialize() {
        new Thread(this::populatePackListAsync).start();
        Platform.runLater(this::initWebView);
        instance = this;
        notificationBox.setOpacity(0);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                          //
        //                                              SETTINGS INIT                                               //
        //                                                                                                          //
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ImageView settingsImage = new ImageView();
        settingsImage.setFitHeight(50);
        settingsImage.setFitWidth(50);
        settingsImage.setImage(MiscUtils.getImage(Internal.ICONS, "settings.png"));
        settingsButton.setGraphic(settingsImage);
        settingsButton.setOnMouseClicked(event -> {
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
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserStyleSheetLocation(MiscUtils.getResourcePath(Internal.CSS_HTML, "webEngine.css"));
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

    private void populatePackListAsync(){
        ObservableList<Pack> packs = FXCollections.observableArrayList();
        packs.addAll(PackRegistry.getPacks());
        packList.getStyleClass().add(CssClasses.PACKLIST);
        Platform.runLater(()->packList.getChildren().clear());
        Platform.runLater(()->packs.forEach(pack -> packList.getChildren().add(new PackCell(pack))));
        if (Internal.VERBOSE) System.out.println("Packlist built!");
    }

    private void setKeyTypedEvent(KeyEvent event) {
        if (!loginBar.getActivePackCell().isPresent()) {
            playButton.setDisable(true);
            return;
        }

        if (!MiscUtils.isEmptyOrNull(usernameField.getText().trim(), passwordField.getText().trim())) {
            playButton.setDisable(false);
            playButton.setOnAction(e -> loginBar.getActionButton().fire());
        } else playButton.setDisable(true);
    }

    public TextFlow getNotificationBox() {
        return notificationBox;
    }

    public LoginBar getLoginBar() {
        return loginBar;
    }

}
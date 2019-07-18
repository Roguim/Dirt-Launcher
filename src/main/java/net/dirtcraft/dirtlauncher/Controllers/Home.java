package net.dirtcraft.dirtlauncher.Controllers;


import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.components.DiscordPresence;
import net.dirtcraft.dirtlauncher.backend.config.CssClasses;
import net.dirtcraft.dirtlauncher.backend.config.Directories;
import net.dirtcraft.dirtlauncher.backend.config.Internal;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.objects.Pack;
import net.dirtcraft.dirtlauncher.backend.utils.FileUtils;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.backend.utils.RamUtils;
import net.dirtcraft.dirtlauncher.elements.LoginBar;
import net.dirtcraft.dirtlauncher.elements.PackCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home {

    private static Home instance;

    @FXML
    private VBox packList;

    @FXML
    private WebView webView;

    @FXML
    private TextFlow notificationBox;

    @FXML
    private Button settingsButton;

    @FXML
    private LoginBar loginBar;
    private PasswordField passwordField;
    private TextField usernameField;
    private Button playButton;
    private final Logger logger;

    public Home(){
        logger = Main.getLogger();
    }

    public static Home getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;
        notificationBox.setOpacity(0);
        Future<ObservableList<Pack>> packs = PackRegistry.getPacksAsync();

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
            Stage stage = Settings.getInstance().getStage();
            stage.show();
            stage.setOnCloseRequest(e -> {
                final File currentGameDirectory = Directories.getGameDirectory();
                final File changedGameDirectory = new File(Settings.getInstance().getGameDirectoryField().getText());
                final JsonObject config = FileUtils.readJsonFromFile(Directories.getConfiguration());

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getMinimumRam().getText())) config.addProperty("minimum-ram", RamUtils.getMinimumRam() * 1024);
                else config.addProperty("minimum-ram", Integer.valueOf(Settings.getInstance().getMinimumRam().getText()));

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getMaximumRam().getText())) config.addProperty("maximum-ram", RamUtils.getRecommendedRam() * 1024);
                else config.addProperty("maximum-ram", Integer.valueOf(Settings.getInstance().getMaximumRam().getText()));

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getJavaArguments().getText())) config.addProperty("java-arguments", Internal.DEFAULT_JAVA_ARGS);
                else config.addProperty("java-arguments", Settings.getInstance().getJavaArguments().getText());

                if (MiscUtils.isEmptyOrNull(Settings.getInstance().getGameDirectoryField().getText())) config.addProperty("game-directory", Directories.getLauncherDirectory().getPath());
                else config.addProperty("game-directory", Settings.getInstance().getGameDirectoryField().getText());

                FileUtils.writeJsonToFile(Directories.getConfiguration(), config);

                if (!Objects.equals(currentGameDirectory, changedGameDirectory)){
                    for(File file : Objects.requireNonNull(currentGameDirectory.listFiles())) {
                        try {
                            FileUtils.moveDirectory(file, changedGameDirectory, true);
                        } catch (IOException exception) {
                            logger.error(exception);
                        }
                    }
                    //noinspection ResultOfMethodCallIgnored
                    currentGameDirectory.delete();
                    FileUtils.initGameDirectory();
                }
            });
        });


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                                                                                          //
        //                                              WEBVIEW INIT                                                //
        //                                                                                                          //
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserStyleSheetLocation(MiscUtils.getResourcePath(Internal.CSS_HTML, "webEngine.css"));
        webEngine.load("https://dirtcraft.net/launcher/");
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("^https?://(store.|)dirtcraft.net");
            if (!(newValue == Worker.State.SUCCEEDED)) return;
            EventListener listener =  e -> {
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
                Matcher matcher = null;
                HTMLAnchorElement hyperlink = (HTMLAnchorElement) lista.item(i);
                if (hyperlink.getHref() != null) matcher = pattern.matcher(hyperlink.getHref());
                if (matcher != null && matcher.find() ) continue;
                ((EventTarget) lista.item(i)).addEventListener("click", listener, false);
            }
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


        //                  PACKLIST INIT
        packList.getStyleClass().add(CssClasses.PACKLIST);
        packList.getChildren().clear();
        try {
            packs.get().forEach(pack -> packList.getChildren().add(new PackCell(pack)));
        } catch (InterruptedException | ExecutionException e){
            logger.warn(e);
            logger.warn("Pack list async failed, trying again synchronized");
            ObservableList<Pack> packsSync = FXCollections.observableArrayList();
            packsSync.addAll(PackRegistry.getPacks());
            packsSync.forEach(pack -> packList.getChildren().add(new PackCell(pack)));
        }
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
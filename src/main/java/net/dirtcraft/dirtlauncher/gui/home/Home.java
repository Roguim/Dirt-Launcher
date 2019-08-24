package net.dirtcraft.dirtlauncher.gui.home;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.game.LaunchGame;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.PackList;
import net.dirtcraft.dirtlauncher.gui.home.toolbar.ToolBar;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.gui.home.login.LoginBar;
import net.dirtcraft.dirtlauncher.gui.home.login.NotificationBox;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.net.URI;

public class Home extends Scene {
    private Stage stage;
    final private LoginBar loginBar;
    final private NotificationBox loginNotification;
    final private AnchorPane root;
    final private PackList sidebar;

    public Home() {
        super(new AnchorPane(), Main.screenDimension.getWidth() / 1.15, Main.screenDimension.getHeight() / 1.35);
        stage = null;
        sidebar = new PackList();

        final FlowPane titleBox = new FlowPane();
        AnchorPane.setTopAnchor(titleBox, 0d);
        AnchorPane.setLeftAnchor(titleBox, 0d);
        MiscUtils.setAbsoluteSize(titleBox, 300, 100);
        titleBox.getStyleClass().add(Constants.CSS_CLASS_TITLE);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().add(new Text("Dirt Launcher"));

        loginBar = new LoginBar();
        final FlowPane actionBox = new FlowPane();
        actionBox.getChildren().add(loginBar);
        actionBox.setOrientation(Orientation.VERTICAL);
        actionBox.setAlignment(Pos.TOP_CENTER);
        actionBox.setPickOnBounds(false);
        MiscUtils.setAbsoluteHeight(actionBox, 115);
        AnchorPane.setLeftAnchor(actionBox, 300d);
        AnchorPane.setRightAnchor(actionBox, 00d);
        AnchorPane.setBottomAnchor(actionBox, 0d);

        loginNotification = new NotificationBox();
        final FlowPane notificationArea = new FlowPane();
        notificationArea.getChildren().add(loginNotification);
        notificationArea.setOrientation(Orientation.VERTICAL);
        notificationArea.setAlignment(Pos.TOP_CENTER);
        notificationArea.setPickOnBounds(false);
        AnchorPane.setTopAnchor(notificationArea, 0d);
        AnchorPane.setLeftAnchor(notificationArea, 300d);
        AnchorPane.setRightAnchor(notificationArea, 00d);
        AnchorPane.setBottomAnchor(notificationArea, 115d);

        final FlowPane sidebarBacking = new FlowPane();
        sidebarBacking.setId(Constants.CSS_ID_PACKLIST_BG);
        MiscUtils.setAbsoluteWidth(sidebarBacking, 300);
        AnchorPane.setTopAnchor(sidebarBacking, 100d);
        AnchorPane.setLeftAnchor(sidebarBacking, 0d);
        AnchorPane.setBottomAnchor(sidebarBacking, 0d);

        final ToolBar toolbar = new ToolBar();
        AnchorPane.setTopAnchor(toolbar, 0d);
        AnchorPane.setRightAnchor(toolbar, 0d);

        root = (AnchorPane) getRoot();
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Home", "Global.css"));
        root.getChildren().addAll(titleBox, sidebarBacking, sidebar, actionBox, notificationArea, toolbar);
    }

    public Stage getStage(){
        if (stage == null) {
            stage = new Stage();
            root.getChildren().add(1, webArea());
            stage.setScene(this);
            stage.setTitle("Dirt Launcher");
            stage.getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "main.png"));
            stage.initStyle(StageStyle.DECORATED);
            stage.setOnCloseRequest(event -> {
                if (!LaunchGame.isGameRunning) Platform.exit();
            });
        }
        return stage;
    }

    private WebView webArea() {
        WebView webView = new WebView();
        AnchorPane.setTopAnchor(webView, 0d);
        AnchorPane.setLeftAnchor(webView, 300d);
        AnchorPane.setRightAnchor(webView, 0d);
        AnchorPane.setBottomAnchor(webView, 0d);
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
                    if (e.getCancelable()) {
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
        return webView;
    }

    public void reload(){
        loginBar.setInputs();
        sidebar.updatePacksAsync();
    }

    public LoginBar getLoginBar() {
        return loginBar;
    }

    public NotificationBox getNotificationBox() {
        return loginNotification;
    }
}

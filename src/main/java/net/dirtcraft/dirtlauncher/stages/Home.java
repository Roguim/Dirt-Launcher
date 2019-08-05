package net.dirtcraft.dirtlauncher.stages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.Main;
import net.dirtcraft.dirtlauncher.backend.game.LaunchGame;
import net.dirtcraft.dirtlauncher.backend.jsonutils.PackRegistry;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;
import net.dirtcraft.dirtlauncher.nodes.Pack;
import net.dirtcraft.dirtlauncher.nodes.loginarea.LoginBar;
import net.dirtcraft.dirtlauncher.nodes.loginarea.NotificationBox;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Home extends Stage {
    private static Home instance;
    private LoginBar loginBar;
    private NotificationBox loginNotification;
    private Home(Scene scene, LoginBar loginBar, NotificationBox notificationBox){
        instance = this;
        setScene(scene);
        this.loginBar = loginBar;
        this.loginNotification = notificationBox;

        setTitle("Dirt Launcher");
        getIcons().setAll(MiscUtils.getImage(Constants.JAR_ICONS, "main.png"));
        initStyle(StageStyle.DECORATED);
        setOnCloseRequest(event -> {
            if (!LaunchGame.isGameRunning) Platform.exit();
        });
    }

    public NotificationBox getNotificationBox() {
        return loginNotification;
    }

    public LoginBar getLoginBar() {
        return loginBar;
    }

    public static Home getInstance() {
        return instance;
    }

    public static class Builder{
        private Scene scene;
        private LoginBar loginBar;
        private NotificationBox loginNotification;
        private AnchorPane root;

        public Builder() {
            long x = System.currentTimeMillis();
            FlowPane titleBox = getTitleBox();
            ScrollPane sidebar = getSidebar();
            FlowPane actionBox = getActionBox();
            FlowPane notificationArea = getNotificationArea();

            FlowPane sidebarBacking = new FlowPane();
            sidebarBacking.getStyleClass().add(Constants.CSS_CLASS_PACKLIST);
            sidebarBacking.getStyleClass().add(Constants.CSS_CLASS_PACKLIST_BG);
            MiscUtils.setAbsoluteWidth(sidebarBacking, 300);
            AnchorPane.setTopAnchor(sidebarBacking, 100d);
            AnchorPane.setLeftAnchor(sidebarBacking, 0d);
            AnchorPane.setBottomAnchor(sidebarBacking, 0d);

            short toolbarUpperWidth = 45;
            short toolbarLowerWidth = 30;
            short toolbarUpperHeight = 45;
            short largeButtonSize = 40;
            short smallButtonSize = 25;
            short smallButtonSpacing = 5;
            short buttonGraphicPadding = 5;

            Button settings = new Button();
            settings.setGraphic(MiscUtils.getGraphic(largeButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "settings.png"));
            MiscUtils.setAbsoluteSize(settings, largeButtonSize, largeButtonSize);
            settings.getStyleClass().add(Constants.CSS_CLASS_TOOLBAR_BUTTON);
            settings.setLayoutX(toolbarUpperWidth - largeButtonSize);
            settings.setOnMouseClicked(event -> {
                Stage stage = net.dirtcraft.dirtlauncher.stages.Settings.getInstance().getStage();
                stage.show();
                stage.setOnCloseRequest(e -> {
                    final int minimumRam = Integer.valueOf(Settings.getInstance().getMinimumRam().getText());
                    final int maximumRam = Integer.valueOf(Settings.getInstance().getMaximumRam().getText());
                    final String gameDirectory = Settings.getInstance().getGameDirectoryField().getText();
                    final String javaArguments = Settings.getInstance().getJavaArguments().getText();
                    Main.getConfig().updateSettings(minimumRam, maximumRam, javaArguments, gameDirectory);
                });
            });

            Button accounts = new Button();
            accounts.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "account.png"));
            accounts.setOnAction(e -> new AccountList().show());

            Button refresh = new Button();
            refresh.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "refresh.png"));
            refresh.setOnAction(event -> {
                try {
                    if (Update.hasUpdate()) Platform.runLater(Update::showStage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Button info = new Button();
            info.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "info.png"));

            Pane toolbarUpper = new Pane();
            toolbarUpper.getStyleClass().add(Constants.CSS_CLASS_TOOLBAR_UPPER);
            MiscUtils.setAbsoluteSize(toolbarUpper, toolbarUpperWidth, toolbarUpperHeight);
            toolbarUpper.getChildren().add(settings);


            Pane toolbarLower = new Pane();
            toolbarLower.getStyleClass().add(Constants.CSS_CLASS_TOOLBAR_LOWER);
            toolbarLower.setLayoutX(toolbarUpperWidth - toolbarLowerWidth);
            toolbarLower.setLayoutY(toolbarUpperHeight);
            toolbarLower.getChildren().addAll(accounts, refresh, info);
            ObservableList<Node> buttons = toolbarLower.getChildren();
            MiscUtils.setAbsoluteSize(toolbarLower, toolbarLowerWidth, (smallButtonSize + smallButtonSpacing) * buttons.size());

            for (int i = 0; i < buttons.size(); i++) {
                Button item = (Button) buttons.get(i);
                item.setCursor(Cursor.HAND);
                item.getStyleClass().add(Constants.CSS_CLASS_TOOLBAR_BUTTON_SMALL);
                item.setLayoutY((smallButtonSize + smallButtonSpacing) * i);
                item.setLayoutX(toolbarLowerWidth - smallButtonSize);
                MiscUtils.setAbsoluteSize(item, smallButtonSize, smallButtonSize);
            }

            System.out.println(System.currentTimeMillis() - x);

            Pane toolbar = new Pane();
            toolbar.getChildren().addAll(toolbarUpper, toolbarLower);
            AnchorPane.setTopAnchor(toolbar, 0d);
            AnchorPane.setRightAnchor(toolbar, 0d);

            root = new AnchorPane();
            root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Sidebar.css"));
            root.getChildren().addAll(titleBox, sidebarBacking, sidebar, actionBox, notificationArea, toolbar);

            scene = new Scene(root, MiscUtils.screenDimension.getWidth() / 1.15, MiscUtils.screenDimension.getHeight() / 1.35);

        }

        private WebView webArea(){
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
            return webView;
        }

        private FlowPane getNotificationArea(){

            loginNotification = new NotificationBox();

            FlowPane notificationArea = new FlowPane();
            notificationArea.getChildren().add(loginNotification);
            notificationArea.setOrientation(Orientation.VERTICAL);
            notificationArea.setAlignment(Pos.TOP_CENTER);
            AnchorPane.setTopAnchor(notificationArea, 0d);
            AnchorPane.setLeftAnchor(notificationArea, 300d);
            AnchorPane.setRightAnchor(notificationArea, 00d);
            AnchorPane.setBottomAnchor(notificationArea, 115d);
            return  notificationArea;
        }

        private ScrollPane getSidebar(){
            VBox packs = new VBox();
            updatePacksAsync(packs);
            packs.setFocusTraversable(false);
            packs.setAlignment(Pos.TOP_CENTER);

            ScrollPane sidebar = new ScrollPane();
            sidebar.setFitToWidth(true);
            sidebar.setFocusTraversable(false);
            sidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sidebar.setPannable(true);
            MiscUtils.setAbsoluteWidth(sidebar, 300);
            AnchorPane.setTopAnchor(sidebar, 100d);
            AnchorPane.setLeftAnchor(sidebar, 0d);
            AnchorPane.setBottomAnchor(sidebar, 0d);
            sidebar.getStyleClass().add(Constants.CSS_CLASS_PACKLIST);
            sidebar.getStyleClass().add(Constants.CSS_CLASS_PACKLIST_SROLL);
            sidebar.setContent(packs);

            return sidebar;
        }

        private FlowPane getActionBox(){
            loginBar = new LoginBar();

            FlowPane actionBox = new FlowPane();
            actionBox.getChildren().add(loginBar);
            actionBox.setOrientation(Orientation.VERTICAL);
            actionBox.setAlignment(Pos.TOP_CENTER);
            MiscUtils.setAbsoluteHeight(actionBox,115);
            AnchorPane.setLeftAnchor(actionBox, 300d);
            AnchorPane.setRightAnchor(actionBox, 00d);
            AnchorPane.setBottomAnchor(actionBox, 0d);

            return actionBox;
        }


        private FlowPane getTitleBox(){
            FlowPane title = new FlowPane();
            AnchorPane.setTopAnchor(title, 0d);
            AnchorPane.setLeftAnchor(title, 0d);
            MiscUtils.setAbsoluteSize(title, 300, 100);
            title.getStyleClass().add(Constants.CSS_CLASS_TITLE);
            title.setAlignment(Pos.CENTER);
            title.getChildren().add(new Text("Dirt Launcher"));

            return title;
        }

        private CompletableFuture updatePacksAsync(VBox packList){
            return CompletableFuture.runAsync(()-> {
                ObservableList<Pack> packs = FXCollections.observableArrayList();
                packs.addAll(PackRegistry.getPacks());
                packList.getStyleClass().add(Constants.CSS_CLASS_PACKLIST);
                Platform.runLater(() -> {
                    packList.getChildren().clear();
                    packList.getChildren().addAll(packs);
                });
                if (Constants.VERBOSE) System.out.println("Packlist built!");
            });
        }

        public Home build(){
            Platform.runLater(()->root.getChildren().add(1, webArea()));
            return new Home(scene, loginBar, loginNotification);
        }
    }
}

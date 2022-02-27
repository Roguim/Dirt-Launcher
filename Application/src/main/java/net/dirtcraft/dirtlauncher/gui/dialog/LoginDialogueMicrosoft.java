package net.dirtcraft.dirtlauncher.gui.dialog;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.gui.home.login.LoginBar;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.dirtcraft.dirtlauncher.configuration.Constants.MICROSOFT_LOGIN_REDIRECT_SUFFIX;
import static net.dirtcraft.dirtlauncher.configuration.Constants.MICROSOFT_LOGIN_URL;

public class LoginDialogueMicrosoft extends Stage {

    public static void grabToken(Consumer<String> tokenConsumer) {
        LoginDialogueMicrosoft dialog = new LoginDialogueMicrosoft(tokenConsumer);
        dialog.show();
    }

    private String token;

    private LoginDialogueMicrosoft(Consumer<String> tokenConsumer) {
        final LoginBar bar = DirtLauncher.getHome().getLoginBar();
        final FlowPane title = new FlowPane();
        title.getChildren().add(new Text("Login"));
        title.getChildren().get(0).setTranslateY(3);
        title.setAlignment(Pos.CENTER);
        title.getStyleClass().add(Constants.CSS_CLASS_TITLE);

        WebView webView = new WebView();
        webView.getEngine().load(MICROSOFT_LOGIN_URL);
        webView.getEngine().setJavaScriptEnabled(true);
        webView.setPrefHeight(406);
        webView.setPrefWidth(406);

        // listen for token
        webView.getEngine().getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
            if (c.next() && c.wasAdded()) {
                for (WebHistory.Entry entry : c.getAddedSubList()) {
                    if (entry.getUrl().startsWith(MICROSOFT_LOGIN_REDIRECT_SUFFIX)) {
                        this.token = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1, entry.getUrl().indexOf("&"));
                        this.hide();
                        CompletableFuture.runAsync(()->{
                            tokenConsumer.accept(token);
                            Platform.runLater(bar::setInputs);
                        }, DirtLauncher.getIOExecutor());
                    }
                }
            }
        });

        final AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(title, 000d);
        AnchorPane.setLeftAnchor(title, 000d);
        AnchorPane.setRightAnchor(title, 000d);
        root.setBackground(Background.EMPTY);
        root.getStylesheets().add(MiscUtils.getCssPath(Constants.JAR_CSS_FXML, "ErrorWindow", "Global.css"));
        root.getChildren().addAll(title, webView);

        final Scene scene = new Scene(root);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Error");

        setWidth(406);
        setMaxHeight(406);
    }

}

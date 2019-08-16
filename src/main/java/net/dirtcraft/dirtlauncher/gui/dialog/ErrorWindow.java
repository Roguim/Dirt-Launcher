package net.dirtcraft.dirtlauncher.gui.dialog;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.gui.home.Home;
import net.dirtcraft.dirtlauncher.gui.home.sidebar.Pack;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ErrorWindow extends Stage {
    public ErrorWindow(String error) {
        final Optional<Pack> pack = Home.getInstance().getLoginBar().getActivePackCell();
        final FlowPane title = new FlowPane();
        title.getChildren().add(new Text("Error"));
        title.getChildren().get(0).setTranslateY(3);
        title.setAlignment(Pos.CENTER);
        title.getStyleClass().add(Constants.CSS_CLASS_TITLE);
        title.setMinHeight(20);
        title.setMaxHeight(20);

        final Text name = new Text("Pack: " + (pack.isPresent() ? pack.get().getName() : "N/A"));
        final Text packVersion = new Text("\tVersion: " + (pack.isPresent() ? pack.get().getVersion() : "N/A"));
        final Text gameVersion = new Text("\nGame Version: " + (pack.isPresent() ? pack.get().getGameVersion() : "N/A"));
        final Text forgeVersion = new Text("\tForge Version: " + (pack.isPresent() ? pack.get().getForgeVersion() : "N/A"));
        final Text osType = new Text("\nOperating System: " + SystemUtils.OS_NAME);
        final Text javaCheck = new Text("\tJRE8 Detected: " + SystemUtils.IS_JAVA_1_8);
        final Text errorOutput = new Text("\n\n" + error);

        final TextFlow textFlow = new TextFlow();
        textFlow.setId("Text");
        textFlow.getChildren().addAll(name, packVersion, gameVersion, forgeVersion, osType, javaCheck, errorOutput);

        final ScrollPane contentInner = new ScrollPane();
        contentInner.setContent(textFlow);
        contentInner.setFitToWidth(true);
        contentInner.setPannable(true);
        contentInner.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        AnchorPane.setTopAnchor(contentInner, 030d);
        AnchorPane.setLeftAnchor(contentInner, 04d);
        AnchorPane.setRightAnchor(contentInner, 04d);
        AnchorPane.setBottomAnchor(contentInner, 04d);

        FlowPane contentOuter = new FlowPane();
        contentOuter.setId("ContentOuter");
        AnchorPane.setTopAnchor(contentOuter, 030d);
        AnchorPane.setLeftAnchor(contentOuter, 000d);
        AnchorPane.setRightAnchor(contentOuter, 000d);
        AnchorPane.setBottomAnchor(contentOuter, 000d);

        final AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(title, 000d);
        AnchorPane.setLeftAnchor(title, 000d);
        AnchorPane.setRightAnchor(title, 000d);
        root.setBackground(Background.EMPTY);
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "ErrorWindow", "Global.css"));
        root.getChildren().addAll(title, contentOuter, contentInner);

        textFlow.setMinHeight(contentInner.getHeight());

        final Scene scene = new Scene(root);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Error");

        setWidth(362);
        setMaxHeight(500);


        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });

        CompletableFuture.runAsync(()->{
            while (textFlow.getHeight() == 0){
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {}
            }
            final double contentSize = textFlow.getHeight() > 470 ? 470 : textFlow.getHeight();
            setHeight( contentSize + 40 );
        });
    }
}

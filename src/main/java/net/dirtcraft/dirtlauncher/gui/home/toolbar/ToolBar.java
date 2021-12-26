package net.dirtcraft.dirtlauncher.gui.home.toolbar;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.dirtcraft.dirtlauncher.DirtLauncher;
import net.dirtcraft.dirtlauncher.configuration.Constants;
import net.dirtcraft.dirtlauncher.gui.dialog.Update;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

final public class ToolBar extends Pane {

    public ToolBar(){
        final short toolbarUpperWidth = 45;
        final short toolbarLowerWidth = 30;
        final short toolbarUpperHeight = 45;
        final short largeButtonSize = 40;
        final short smallButtonSize = 25;
        final short smallButtonSpacing = 5;
        final short buttonGraphicPadding = 5;

        final Button settings = new Button();
        settings.setCursor(Cursor.HAND);
        settings.setGraphic(MiscUtils.getGraphic(largeButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "settings.png"));
        MiscUtils.setAbsoluteSize(settings, largeButtonSize, largeButtonSize);
        settings.setLayoutX(toolbarUpperWidth - largeButtonSize);
        settings.setOnMouseClicked(event -> {
            Stage stage = DirtLauncher.getSettingsMenu().getStage();
            stage.show();
        });

        final Button accounts = new Button();
        accounts.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "account.png"));
        accounts.setOnAction(e -> new AccountList().show());

        final Button refresh = new Button();
        refresh.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "refresh.png"));
        refresh.setOnAction(event -> {
            CompletableFuture.runAsync(()-> DirtLauncher.getConfig().fullReload()).thenRun(() -> {
                DirtLauncher.getHome().update();
                try {
                    if (Update.hasUpdate()) Platform.runLater(Update::showStage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        final Button debug = new Button();
        debug.setOnAction(e -> MiscUtils.updateLauncher());

        final Button info = new Button();
        info.setGraphic(MiscUtils.getGraphic(smallButtonSize - buttonGraphicPadding, Constants.JAR_ICONS, "info.png"));
        info.setOnAction(event -> new About().show());

        final Pane toolbarUpper = new Pane();
        toolbarUpper.setId(Constants.CSS_ID_TOOLBAR_UPPER);
        MiscUtils.setAbsoluteSize(toolbarUpper, toolbarUpperWidth, toolbarUpperHeight);
        toolbarUpper.getChildren().add(settings);


        final Pane toolbarLower = new Pane();
        toolbarLower.setId(Constants.CSS_ID_TOOLBAR_LOWER);
        toolbarLower.setLayoutX(toolbarUpperWidth - toolbarLowerWidth);
        toolbarLower.setLayoutY(toolbarUpperHeight - 5);
        //toolbarLower.getChildren().addAll(accounts, refresh, info);
        toolbarLower.getChildren().addAll(accounts, info, refresh);
        if (Constants.DEBUG) toolbarLower.getChildren().add(debug);

        ObservableList<Node> buttons = toolbarLower.getChildren();
        MiscUtils.setAbsoluteSize(toolbarLower, toolbarLowerWidth, (smallButtonSize + smallButtonSpacing) * buttons.size() + 5 );

        for (int i = 0; i < buttons.size(); i++) {
            Button item = (Button) buttons.get(i);
            item.setCursor(Cursor.HAND);
            item.setLayoutY((smallButtonSize + smallButtonSpacing) * i + 5);
            item.setLayoutX(toolbarLowerWidth - smallButtonSize);
            MiscUtils.setAbsoluteSize(item, smallButtonSize, smallButtonSize);
        }

        getChildren().addAll(toolbarLower, toolbarUpper);
    }

}

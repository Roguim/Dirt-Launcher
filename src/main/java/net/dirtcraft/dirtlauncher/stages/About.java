package net.dirtcraft.dirtlauncher.stages;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

public class About extends Stage {
    public About() {
        final TextFlow title = new TextFlow();
        title.getChildren().add(new Text("About"));
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add(Constants.CSS_CLASS_TITLE);
        MiscUtils.setAbsoluteHeight(title, 20);

        final AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(title, 000d);
        AnchorPane.setLeftAnchor(title, 010d);
        AnchorPane.setRightAnchor(title, 010d);
        //AnchorPane.setBottomAnchor(title, vBoxSize + 20);
        root.setBackground(Background.EMPTY);
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Accounts", "Global.css"));
        root.getChildren().addAll(title);

        final Scene scene = new Scene(root, 292, 500);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Accounts");


        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });
    }
}

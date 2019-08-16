package net.dirtcraft.dirtlauncher.gui.home.toolbar;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.utils.Constants;
import net.dirtcraft.dirtlauncher.utils.MiscUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class About extends Stage {
    About() {
        final FlowPane title = new FlowPane();
        title.getChildren().add(new Text("About"));
        title.getChildren().get(0).setTranslateY(3);
        title.setAlignment(Pos.CENTER);
        title.getStyleClass().add(Constants.CSS_CLASS_TITLE);
        MiscUtils.setAbsoluteHeight(title, 20);

        StringBuilder EulaText = new StringBuilder();
        try(
                InputStream is = MiscUtils.getResourceStream("LICENSE.md");
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
        ){
            String buffer;
            while ((buffer = br.readLine()) != null){
                EulaText.append(buffer);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        final Text version = new Text("Dirt-Launcher Version: " + Constants.LAUNCHER_VERSION);
        final Text contributors = new Text("\nAuthors: " + Constants.AUTHORS);
        final Text specialMentions = new Text("\nSpecial Thanks: " + Constants.HELPERS);
        final Text eula = new Text("\n\n" + EulaText.toString());

        final TextFlow contentInner = new TextFlow();
        contentInner.setId("ContentInner");
        AnchorPane.setTopAnchor(contentInner, 030d);
        AnchorPane.setLeftAnchor(contentInner, 04d);
        AnchorPane.setRightAnchor(contentInner, 04d);
        AnchorPane.setBottomAnchor(contentInner, 04d);
        contentInner.getChildren().addAll(version, contributors, specialMentions, eula);


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
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "About", "Global.css"));
        root.getChildren().addAll(title, contentOuter, contentInner);

        final Scene scene = new Scene(root, 362, 500);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("About");


        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });
    }
}

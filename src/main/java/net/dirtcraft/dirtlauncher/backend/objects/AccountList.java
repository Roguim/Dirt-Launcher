package net.dirtcraft.dirtlauncher.backend.objects;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

final public class AccountList extends Stage {
    private final AccountList instance = this;
    private final ScrollPane scrollPane;
    public AccountList(){
        VBox backing = new VBox();
        backing.setBackground(Background.EMPTY);
        backing.setAlignment(Pos.TOP_CENTER);
        backing.getStyleClass().add(Constants.CSS_CLASS_SCROLLPANE_VBOX);

        TextFlow title = new TextFlow();
        title.getChildren().add(new Text("Accounts:"));
        title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add(Constants.CSS_CLASS_TITLE);

        scrollPane = new ScrollPane();
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setMinHeight(450);
        scrollPane.setMaxHeight(450);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(backing);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);

        AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(title, 000d);
        AnchorPane.setLeftAnchor(title, 010d);
        AnchorPane.setRightAnchor(title, 010d);
        AnchorPane.setBottomAnchor(title, 470d);
        AnchorPane.setTopAnchor(scrollPane, 30d);
        AnchorPane.setLeftAnchor(scrollPane, 10d);
        AnchorPane.setRightAnchor(scrollPane, 10d);
        AnchorPane.setBottomAnchor(scrollPane, 05d);
        root.setMinHeight(500);
        root.setMaxHeight(500);
        root.setBackground(Background.EMPTY);
        root.getChildren().addAll(title, scrollPane);
        root.getStyleClass().add(Constants.CSS_CLASS_ACCOUNTLIST);
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Accounts", "Global.css"));

        Scene scene = new Scene(root, 292, 500);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Accounts");

        for (byte i = 0; i < 16; i++){
            backing.getChildren().add(new Account());
        }

    }

    private class Account extends Button {
        //private final Session session;
        private double lastDragY;
        Account(){//(Session session){
            //this.session = session;
            setOnMouseDragged(event->{
                if (event.isPrimaryButtonDown()) {
                    double change = (lastDragY - event.getY()) / scrollPane.getHeight();
                    scrollPane.setVvalue(scrollPane.getVvalue() + change);
                    lastDragY = change;
                }
            });
        }
        @Override
        public void fire() {
            //Home.getInstance().getLoginBar().setSession(session);
            instance.close();
        }
    }
}

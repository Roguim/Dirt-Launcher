package net.dirtcraft.dirtlauncher.backend.objects;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.cydhra.nidhogg.data.Session;
import net.dirtcraft.dirtlauncher.Controllers.Home;
import net.dirtcraft.dirtlauncher.backend.utils.Constants;
import net.dirtcraft.dirtlauncher.backend.utils.MiscUtils;

final public class AccountList extends Stage {
    private final AccountList instance = this;
    private final ScrollPane root;
    public AccountList(){
        VBox backing = new VBox();
        backing.setBackground(Background.EMPTY);
        backing.setAlignment(Pos.TOP_CENTER);
        backing.getStyleClass().add(Constants.CSS_CLASS_ACCOUNTLIST);

        root = new ScrollPane();
        root.setBackground(Background.EMPTY);
        root.setFitToWidth(true);
        root.setContent(backing);
        root.getStylesheets().add(MiscUtils.getResourcePath(Constants.JAR_CSS_FXML, "Accounts", "Global.css"));
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(root, 250, 500);
        scene.setFill(Paint.valueOf("transparent"));
        setScene(scene);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);
        setTitle("Accounts");

        for (byte i = 0; i < Byte.MAX_VALUE; i++){
            Button button = new Account();
            backing.getChildren().add(button);
        }

    }

    private class Account extends Button {
        //private final Session session;
        private double lastDragY;
        Account(){//(Session session){
            //this.session = session;
            getStyleClass().add(Constants.CSS_CLASS_ACCOUNTLIST_BUTTON);
            setOnMouseDragged(event->{
                if (event.isPrimaryButtonDown()) {
                    double change = (lastDragY - event.getY()) / root.getHeight();
                    root.setVvalue(root.getVvalue() + change);
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

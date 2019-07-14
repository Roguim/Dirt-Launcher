package net.dirtcraft.dirtlauncher.elements;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class LoginBar extends Pane {
    private GridPane loginContainer;
    private TextField usernameField;
    private PasswordField passField;
    private PlayButton actionButton;
    private Types type;

    public LoginBar(){
        passField = new PasswordField();
        usernameField = new TextField();
        actionButton = new PlayButton();
        loginContainer = new GridPane();

        //Force the size - otherwise it changes and that's bad..
        setAbsoluteSize(actionButton , 58 ,  59 );
        setAbsoluteSize(this ,264.0 ,  74 );
        setAbsoluteSize(loginContainer,250.0, 59);

        setId("LoginBar");
        getStyleClass().add("LoginArea");
        getStyleClass().add( "LoginBar");
        passField.setId("PasswordField");
        usernameField.setId("UsernameField");

        RowConstraints x1 = new RowConstraints();
        RowConstraints x2 = new RowConstraints();
        ColumnConstraints y1 = new ColumnConstraints();
        ColumnConstraints y2 = new ColumnConstraints();
        x1.setValignment(VPos.BOTTOM);
        x2.setValignment ( VPos.TOP );
        y2.setHalignment( HPos.LEFT );
        y1.setHalignment(HPos.RIGHT );
        x1.setMinHeight(29);
        x1.setMaxHeight(29);        // This is how u make a grid pane without  \\
        x2.setMinHeight(29);        // scene builder. It's hard work but hey,  \\
        x2.setMaxHeight(29);        // It's an honest living. Also this space  \\
        y1.setMinWidth(190);        // was perfect for a comment block because \\
        y1.setMaxWidth(190);        // it just is screaming for someone to put \\
        y2.setMinWidth( 60);        // something in this exact box shaped area \\
        y2.setMaxWidth( 60);
        loginContainer.getRowConstraints().add(0, x1);
        loginContainer.getRowConstraints().add(1, x2);
        loginContainer.getColumnConstraints().add(0, y1);
        loginContainer.getColumnConstraints().add(1, y2);
        loginContainer.add(actionButton, 1, 0,  1, 2);
        loginContainer.add(usernameField, 0, 0, 1, 1);
        loginContainer.add(passField , 0,  1,  1,  1);
        loginContainer.setLayoutX(8);
        loginContainer.setLayoutY(8);

        usernameField.setPromptText("E-Mail Address");
        passField.setPromptText("Password");
        actionButton.setDefaultButton(true);
        actionButton.setDisable(true);
        actionButton.setText("Play");
        getChildren().add(loginContainer);

    }


    private void setAbsoluteSize(Region node, double width, double height){
        node.setPrefSize(width, height);
        node.setMaxSize(width,  height);
        node.setMinSize(width,  height);
    }

    public PlayButton getActionButton() {
        return actionButton;
    }

    public PasswordField getPassField() {
        return passField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public GridPane getLoginContainer() {
        return loginContainer;
    }


    public Types getType() {
        return type;
    }

    public void setType(Types type) {
        this.type = type;
    }

    public static enum Types{
        INSTALL,
        UPDATE,
        LAUNCH,
        NONE;

        @Override
        public String toString() {
            switch (this){
                case LAUNCH:return "Launch";
                case UPDATE:return "Update";
                case INSTALL:return "Install";
                case NONE:return "N/A";
                default:return "NULL";
            }
        }
    }
}
